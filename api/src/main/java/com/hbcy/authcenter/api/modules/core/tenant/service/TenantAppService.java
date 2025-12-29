package com.hbcy.authcenter.api.modules.core.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.modules.core.app.dao.AppMapper;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourcePermMapper;
import com.hbcy.authcenter.api.modules.core.app.dto.AppCardDTO;
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
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppGrantStatusUpdateVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppGrantUpdateVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppGrantVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 授权应用到租户
 *
 * @author 姚泰然
 * @date 2025-12-23 17:24
 */
@Service
public class TenantAppService extends ServiceImpl<TenantAppMapper, TenantApp> {
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

    private static List<TenantAppResource> genTenantAppResources(
            String appId, Set<String> permIds, String tenantId) {
        List<TenantAppResource> tenantAppResources = new ArrayList<>();
        for (String filteredId : permIds) {
            TenantAppResource tenantAppResource = new TenantAppResource();
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
     * 授权应用
     * 新建或修改已有的
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
        String currentBinding = app.getBindingTenant();
        if (App.BINDING_PLACEHOLDER.equals(currentBinding)) {
            //单租户应用绑定租户
            app.setBindingTenant(tenantId);
            app.setUpdateUser(UserContextUtils.getUserId());
            appMapper.updateById(app);
        } else if (!"".equals(currentBinding) && !tenantId.equals(currentBinding)) {
            throw new ParamError("单租户应用已绑定其他租户");
        }
        TenantApp tenantApp = new TenantApp();
        tenantApp.setTenantId(tenantId);
        tenantApp.setAppId(vo.getAppId());
        tenantApp.setForbidden(0);
        tenantApp.setGrantAll(vo.isGrantAll() ? 1 : 0);
        tenantApp.setCreateUser(UserContextUtils.getUserId());
        tenantApp.setUpdateUser(UserContextUtils.getUserId());

        //先删除已有的关联再批量插入
        tenantAppResourceMapper.delete(new QueryWrapper<TenantAppResource>()
                .eq(TenantAppResource.COL_TENANT_ID, tenantId)
                .eq(TenantAppResource.COL_APP_ID, vo.getAppId()));
        if (!vo.isGrantAll()) {
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
        save(inst);
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
        this.updateById(grant);
    }

    /**
     * 查看租户已授权的应用列表
     */
    public List<AppCardDTO> listGrantApps(String tenantId) {
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
}
