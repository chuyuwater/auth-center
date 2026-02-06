package com.hbcy.authcenter.api.modules.core.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.core.app.dao.AppMapper;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourcePermMapper;
import com.hbcy.authcenter.api.modules.core.app.dto.GrantAppDTO;
import com.hbcy.authcenter.api.modules.core.app.model.App;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.api.modules.core.app.vo.BindOrgTreeVO;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitResourceMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitResource;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppResourceMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantAppResource;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppBatchGrantVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppGrantStatusUpdateVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppGrantUpdateVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppGrantVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.lock.service.RedissonDistributedLock;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 授权应用到租户
 *
 * @author 姚泰然
 * @date 2025-12-23 17:24
 */
@Service
public class TenantAppService extends ServiceImpl<TenantAppMapper, TenantApp> {
    public static final String GRANT_LOCK = "portal:tenant:app:grant:";
    @Resource
    private AppMapper appMapper;
    @Resource
    private TenantMapper tenantMapper;
    @Resource
    private ResourcePermMapper resourcePermMapper;
    @Resource
    private PermUnitResourceMapper permUnitResourceMapper;
    @Resource
    private OrgTreeMapper orgTreeMapper;
    @Resource
    private TenantAppResourceMapper tenantAppResourceMapper;
    @Resource
    private RedissonDistributedLock redissonDistributedLock;

    private static List<TenantAppResource> genTenantAppResources(
            String appId, Set<String> permIds, String tenantId) {
        List<TenantAppResource> tenantAppResources = new ArrayList<>();
        for (String filteredId : permIds) {
            TenantAppResource tenantAppResource = new TenantAppResource();
            tenantAppResource.setId(UlidCreator.getUlid().toString());
            tenantAppResource.setTenantId(tenantId);
            tenantAppResource.setAppId(appId);
            tenantAppResource.setPermId(filteredId);
            tenantAppResource.setCreateUser(UserContextUtils.getUserId());
            tenantAppResource.setUpdateUser(UserContextUtils.getUserId());
            tenantAppResources.add(tenantAppResource);
        }
        return tenantAppResources;
    }

    /**
     * 创建应用授权
     *
     * @param vo 授权详情
     */
    @Transactional(rollbackFor = Exception.class)
    public void grantApp(TenantAppGrantVO vo) {
        String tenantId = vo.getTenantId();
        App app = appMapper.selectById(vo.getAppId());
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (app == null || tenant == null) {
            throw new ParamError("租户或应用不存在");
        }
        if (app.getForbidden() == 1) {
            throw new ParamError("应用已被禁用");
        }
        if (tenant.getForbidden() == 1) {
            throw new ParamError("租户已被禁用");
        }
        bindAppTenant(app, tenantId);
        TenantApp tenantApp = new TenantApp();
        tenantApp.setTenantId(tenantId);
        tenantApp.setAppId(vo.getAppId());
        tenantApp.setForbidden(0);
        //TODO：这里直接绑定默认组织树，后续由用户自己选择
        tenantApp.setOrgTree(OrgTree.ORG_ID_TEMPLATE.formatted(tenantId, 1));
        tenantApp.setGrantAll(vo.isGrantAll() ? 1 : 0);
        tenantApp.setCreateUser(UserContextUtils.getUserId());
        tenantApp.setUpdateUser(UserContextUtils.getUserId());

        //先删除已有的关联再批量插入
        tenantAppResourceMapper.delete(new QueryWrapper<TenantAppResource>()
                .eq(TenantAppResource.COL_TENANT_ID, tenantId)
                .eq(TenantAppResource.COL_APP_ID, vo.getAppId()));
        if (!vo.isGrantAll() && !CollectionUtils.isEmpty(vo.getPermIds())) {
            //手动勾选的资源
            Set<String> filteredIds = resourcePermMapper.selectList(new QueryWrapper<ResourcePerm>()
                            .eq(ResourcePerm.COL_APP_ID, vo.getAppId())
                            .in(ResourcePerm.COL_ID, vo.getPermIds())
                            .select(ResourcePerm.COL_ID)).stream()
                    .map(ResourcePerm::getId).collect(Collectors.toSet());
            List<TenantAppResource> tenantAppResources = genTenantAppResources(
                    vo.getAppId(), filteredIds, tenantId);
            tenantAppResourceMapper.insert(tenantAppResources);
        }
        try {
            save(tenantApp);
        } catch (DuplicateKeyException e) {
            throw new ParamError("租户已授权该应用");
        }
    }

    /**
     * 修改授权
     * 1. 原来是全部授权，现在也是 -> 无权限影响，直接返回
     * 2. 原来是部分授权，现在是全部授权 -> 移除关联表中的数据
     * 3. 原来是全部授权，现在不是 -> 计算新的授权，插入关联表，计算差值移除角色/策略授权
     * 4. 原来是部分授权，现在也是 -> 同上
     *
     * @param vo 修改内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGrantedApp(String grantId, TenantAppGrantUpdateVO vo) {
        TenantApp inst = baseMapper.selectById(grantId);
        if (inst == null) {
            throw new ParamError("授权已取消");
        }
        //情况1
        if (inst.getGrantAll() > 0 && vo.isGrantAll()) {
            return;
        }
        //情况2
        if (inst.getGrantAll() == 0 && vo.isGrantAll()) {
            tenantAppResourceMapper.delete(new QueryWrapper<TenantAppResource>()
                    .eq(TenantAppResource.COL_TENANT_ID, inst.getTenantId())
                    .eq(TenantAppResource.COL_APP_ID, inst.getAppId()));
            inst.setGrantAll(1);
            save(inst);
            return;
        }
        //情况3/4
        //计算新授权
        Set<String> newPermIds = vo.getPermIds();
        if (CollectionUtils.isEmpty(newPermIds)) {
            newPermIds = new HashSet<>();
        } else {
            newPermIds = resourcePermMapper.filterAppPermIds(inst.getAppId(), newPermIds);
        }
        Set<String> oldPermIds = new HashSet<>();
        if (inst.getGrantAll() > 0) {
            oldPermIds = resourcePermMapper.listAppPermIds(inst.getAppId());
        } else {
            oldPermIds = tenantAppResourceMapper.getGrantedPermIds(inst.getTenantId(), inst.getAppId());
            // 清空授权
            tenantAppResourceMapper.deleteByIds(oldPermIds);
        }
        if (!newPermIds.isEmpty()) {
            List<TenantAppResource> tenantAppResources = genTenantAppResources(
                    inst.getAppId(), newPermIds, inst.getTenantId());
            tenantAppResourceMapper.insert(tenantAppResources);
        }
        //检查权限是否缩小
        oldPermIds.removeAll(newPermIds);
        if (!CollectionUtils.isEmpty(oldPermIds)) {
            //最大授权缩小，需要在租户下的角色、策略中移除所有相关权限
            permUnitResourceMapper.delete(new QueryWrapper<PermUnitResource>()
                    .in(PermUnitResource.COL_PERM_ID, oldPermIds));
        }
        inst.setGrantAll(vo.isGrantAll() ? 1 : 0);
        inst.setUpdateUser(UserContextUtils.getUserId());
        updateById(inst);
    }

    /**
     * 切换授权状态
     */
    public void switchGrantStatus(TenantAppGrantStatusUpdateVO vo) {
        TenantApp grant = getById(vo.getGrantId());
        if (StringUtils.isBlank(grant.getAppId())) {
            throw new ParamError("授权关系不存在");
        }
        if (grant.getForbidden().equals(vo.getForbidden())) {
            return;
        }
        grant.setForbidden(vo.getForbidden());
        grant.setUpdateUser(UserContextUtils.getUserId());
        grant.setUpdateTime(LocalDateTime.now());
        this.updateById(grant);
    }

    /**
     * 查看租户已授权的应用列表
     */
    public List<GrantAppDTO> listGrantApps(String tenantId) {
        return baseMapper.listGrantApps(tenantId);
    }

    /**
     * 租户为应用绑定组织树
     */
    public void bindingOrgTree(BindOrgTreeVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        TenantApp tenantApp = baseMapper.selectOne(new QueryWrapper<TenantApp>()
                .eq(TenantApp.COL_APP_ID, vo.getAppId())
                .eq(TenantApp.COL_TENANT_ID, tenantId)
                .eq(TenantApp.COL_FORBIDDEN, 0)
        );
        if (tenantApp == null) {
            throw new PermissionError();
        }
        OrgTree root = orgTreeMapper.selectById(vo.getOrgRootId());
        if (root == null) {
            throw new ParamError("指定组织树节点不存在");
        }
        if (!root.getTenantId().equals(tenantId)) {
            throw new PermissionError();
        }
        if (!OrgTree.ORG_ID_TEMPLATE.formatted(tenantApp, 0).equals(root.getParentId())) {
            throw new ParamError("必须使用组织树的根节点");
        }
        TenantApp toUpdate = new TenantApp();
        toUpdate.setId(tenantApp.getId());
        toUpdate.setOrgTree(vo.getOrgRootId());
        save(toUpdate);
    }

    /**
     * 删除应用授权
     *
     * @param grantId 授权关系ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteGrant(String grantId) {
        TenantApp tenantApp = baseMapper.selectById(grantId);
        if (tenantApp == null) {
            return;
        }
        baseMapper.update(new UpdateWrapper<TenantApp>()
                .eq(TenantApp.COL_ID, tenantApp.getId())
                .set(TenantApp.COL_UPDATE_USER, UserContextUtils.getUserId())
                .set(TenantApp.COL_DELETE_TIME, System.currentTimeMillis()));
        tenantAppResourceMapper.delete(new QueryWrapper<TenantAppResource>()
                .eq(TenantAppResource.COL_TENANT_ID, tenantApp.getTenantId())
                .eq(TenantAppResource.COL_APP_ID, tenantApp.getAppId()));
        permUnitResourceMapper.delete(new QueryWrapper<PermUnitResource>()
                .eq(PermUnitResource.COL_APP_ID, tenantApp.getAppId())
                .eq(PermUnitResource.COL_TENANT_ID, tenantApp.getTenantId()));
    }

    private void bindAppTenant(App app, String tenantId) {
        String currentBinding = app.getBindingTenant();
        if (App.BINDING_PLACEHOLDER.equals(currentBinding)) {
            //单租户应用绑定租户
            app.setBindingTenant(tenantId);
            app.setUpdateUser(UserContextUtils.getUserId());
            app.setUpdateTime(LocalDateTime.now());
            appMapper.updateById(app);
        } else if (!"".equals(currentBinding) && !tenantId.equals(currentBinding)) {
            throw new ParamError("单租户应用已绑定其他租户");
        }
    }

    @Transactional
    public void tryGrantApp(TenantAppBatchGrantVO vo) {
        boolean ok = redissonDistributedLock.tryLock(GRANT_LOCK + vo.getTenantId(), TimeUnit.SECONDS, 5, 10);
        if (!ok) {
            throw new ParamError("其他人正在使用修改授权，请重试");
        }
        try {
            batchGrantApp(vo);
        } finally {
            redissonDistributedLock.unlock(GRANT_LOCK + vo.getTenantId());
        }
    }

    /**
     * 批量授权时，授权全部权限
     * @param vo 授权信息
     */
    private void batchGrantApp(TenantAppBatchGrantVO vo) {
        String tenantId = vo.getTenantId();
        List<App> appList = appMapper.selectList(new QueryWrapper<App>()
                .eq(App.COL_FORBIDDEN, 0)
                .in(App.COL_ID, vo.getAppIds()));
        Map<String, App> appMap = appList.stream().collect(Collectors.toMap(App::getId, app -> app));
        if (appList.size() < vo.getAppIds().size()) {
            throw new ParamError("部分应用不存在或已被禁用");
        }
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant.getForbidden() == 1) {
            throw new ParamError("租户已被禁用");
        }
        Set<String> granted = baseMapper.selectList(new QueryWrapper<TenantApp>()
                        .eq(TenantApp.COL_TENANT_ID, tenantId)).stream().map(TenantApp::getAppId)
                .collect(Collectors.toSet());
        //需要移除的授权
        Set<String> toDeleteAppIds = new HashSet<>(granted);
        toDeleteAppIds.removeAll(vo.getAppIds());
        Set<String> toAddAppIds = new HashSet<>(vo.getAppIds());
        toAddAppIds.removeAll(granted);
        if (!toDeleteAppIds.isEmpty()) {
            baseMapper.update(new UpdateWrapper<TenantApp>()
                    .eq(TenantApp.COL_TENANT_ID, tenantId)
                    .in(TenantApp.COL_APP_ID, toDeleteAppIds)
                    .set(TenantApp.COL_UPDATE_USER, UserContextUtils.getUserId())
                    .set(TenantApp.COL_DELETE_TIME, System.currentTimeMillis()));
            tenantAppResourceMapper.delete(new QueryWrapper<TenantAppResource>()
                    .eq(TenantAppResource.COL_TENANT_ID, tenantId)
                    .in(TenantAppResource.COL_APP_ID, toDeleteAppIds));
            permUnitResourceMapper.delete(new QueryWrapper<PermUnitResource>()
                    .in(PermUnitResource.COL_APP_ID, toDeleteAppIds)
                    .eq(PermUnitResource.COL_TENANT_ID, tenantId));
        }
        List<TenantApp> tenantApps = new ArrayList<>();
        String userId = UserContextUtils.getUserId();
        for (String appId : toAddAppIds) {
            bindAppTenant(appMap.get(appId), tenantId);
            TenantApp tenantApp = new TenantApp();
            tenantApp.setId(UlidCreator.getUlid().toString());
            tenantApp.setTenantId(tenantId);
            tenantApp.setAppId(appId);
            tenantApp.setOrgTree(OrgTree.ORG_ID_TEMPLATE.formatted(tenantId, 1));
            tenantApp.setCreateUser(userId);
            tenantApp.setUpdateUser(userId);
            tenantApps.add(tenantApp);
        }
        baseMapper.insertIgnore(tenantApps);
    }
}
