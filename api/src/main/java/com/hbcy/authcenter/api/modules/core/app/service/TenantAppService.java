package com.hbcy.authcenter.api.modules.core.app.service;

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
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
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
        if (!vo.isGrantAll()) {
            //手动勾选的资源
            Set<String> filteredIds = resourcePermMapper.selectList(new QueryWrapper<ResourcePerm>()
                            .eq(ResourcePerm.COL_APP_ID, vo.getAppId())
                            .in(ResourcePerm.COL_ID, vo.getPermIds())
                            .select(ResourcePerm.COL_ID)).stream()
                    .map(ResourcePerm::getId).collect(Collectors.toSet());
            tenantApp.setPermIds(filteredIds);
        }
        try {
            save(tenantApp);
        } catch (DuplicateKeyException e) {
            throw new ParamError("租户已授权该应用");
        }
    }

    /**
     * 修改授权
     *
     * @param vo 修改内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGrantedApp(String grantId, TenantAppGrantUpdateVO vo) {
        TenantApp old = baseMapper.selectById(grantId);
        if (old == null) {
            throw new ParamError("授权已取消");
        }
        Set<String> oldPermIds = old.getPermIds();
        Set<String> newPermIds = vo.getPermIds();
        //不再全部授权
        if (old.getGrantAll() > 0 && !vo.isGrantAll()) {
            oldPermIds = resourcePermMapper.selectList(new QueryWrapper<ResourcePerm>()
                            .eq(ResourcePerm.COL_APP_ID, old.getAppId()))
                    .stream().map(ResourcePerm::getId).collect(Collectors.toSet());
        } else if (old.getGrantAll() == 0 && !vo.isGrantAll()) {
            if (oldPermIds == null) oldPermIds = new HashSet<>();
            if (!CollectionUtils.isEmpty(vo.getPermIds())) {
                newPermIds = resourcePermMapper.filterAppPermIds(old.getAppId(), vo.getPermIds());
            }
        }
        oldPermIds.removeAll(newPermIds);
        if (!CollectionUtils.isEmpty(oldPermIds)) {
            //最大授权缩小，需要在租户下的角色、策略中移除所有相关权限
            permUnitResourceMapper.delete(new QueryWrapper<PermUnitResource>()
                    .in(PermUnitResource.COL_PERM_ID, oldPermIds));
        }
        old.setGrantAll(vo.isGrantAll() ? 1 : 0);
        old.setUpdateUser(UserContextUtils.getUserId());
        old.setPermIds(vo.isGrantAll() ? new HashSet<>() : newPermIds);
        save(old);
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
     * 租户已授权的应用ID清单
     * 不含被禁用的
     *
     * @param tenantId 租户id
     * @return 应用id列表
     */
    public List<String> listBindAppIds(String tenantId) {
        return baseMapper.selectList(new QueryWrapper<TenantApp>()
                        .eq(TenantApp.COL_TENANT_ID, tenantId)
                        .eq(TenantApp.COL_FORBIDDEN, 0))
                .stream().map(TenantApp::getAppId).collect(Collectors.toList());
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
