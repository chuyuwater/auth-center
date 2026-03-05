package com.hbcy.authcenter.api.modules.core.inner.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hbcy.authcenter.api.common.bean.NameCacheService;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourcePermMapper;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppResourceMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantAppResource;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.common.web.api.NamedId;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 姚泰然
 * @date 2026-03-05 09:12
 */
@Service
public class SDKService {
    @Resource
    private OrgTreeMapper orgTreeMapper;
    @Resource
    private NameCacheService nameCacheService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TenantMapper tenantMapper;
    @Resource
    private TenantAppMapper tenantAppMapper;
    @Resource
    private ResourcePermMapper resourcePermMapper;
    @Resource
    private TenantAppResourceMapper tenantAppResourceMapper;

    public Map<String, String> getOrgNames(List<String> orgIds, boolean fullName) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return Map.of();
        }
        List<NamedId> namedIds = orgTreeMapper.selectNameByIds(orgIds, fullName);
        return namedIds.stream()
                .collect(Collectors.toMap(NamedId::getItemId, NamedId::getItemName));
    }

    public Map<String, String> getUserNames(Set<String> userIds) {
        return nameCacheService.getUserNameMap(userIds);
    }

    public List<String> listGrantOrgs(String userId, String appId,
                                      String permCode, String parentOrgId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getForbidden() > 0) {
            return List.of();
        }
        //权限码对应的权限id
        List<String> permIds = resourcePermMapper.listPermCodeIds(appId, permCode);
        if (CollectionUtils.isEmpty(permIds)) {
            return List.of();
        }
        String parentIdPath = null;
        if (StringUtils.isNotBlank(parentOrgId)) {
            OrgTree orgTree = orgTreeMapper.selectById(parentOrgId);
            if (orgTree == null) {
                return List.of();
            }
            parentIdPath = orgTree.getIdPath();
        }
        String tenantId = user.getTenantId();
        Tenant tenant = tenantMapper.selectById(tenantId);
        //默认管理员，需要确认租户到底有没有该权限
        if (tenant.getAdminId().equals(userId)) {
            TenantApp tenantApp = tenantAppMapper.selectOne(new QueryWrapper<TenantApp>()
                    .eq(TenantApp.COL_APP_ID, appId)
                    .eq(TenantApp.COL_TENANT_ID, tenantId));
            if (tenantApp == null) {
                return List.of();
            }
            if (tenantApp.getGrantAll() > 0) {
                return orgTreeMapper.getAllOrgIds(tenantId);
            }
            if (tenantAppResourceMapper.exists(new QueryWrapper<TenantAppResource>()
                    .in(TenantAppResource.COL_PERM_ID, permIds)
                    .eq(TenantAppResource.COL_TENANT_ID, tenantId))) {
                return orgTreeMapper.getAllOrgIds(tenantId);
            }
            return List.of();
        }
        return orgTreeMapper.listGrantOrgs(userId, tenantId, permIds, parentIdPath);
    }
}
