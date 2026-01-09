package com.hbcy.authcenter.api.modules.core.inner.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hbcy.authcenter.api.config.UserAuthConfig;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourcePermMapper;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitUserMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppResourceMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import com.hbcy.authcenter.gateway.constants.GatewayConstants;
import com.hbcy.authcenter.gateway.dto.ApiPermDTO;
import com.hbcy.authcenter.gateway.vo.RefreshUserPermVO;
import com.hbcy.common.redis.RedisExtendService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 姚泰然
 * @date 2025-12-31 10:23
 */
@Service
public class InnerService {
    @Resource
    private TenantAppMapper tenantAppMapper;
    @Resource
    private ResourcePermMapper resourcePermMapper;
    @Resource
    private TenantAppResourceMapper tenantAppResourceMapper;
    @Resource
    private PermUnitUserMapper permUnitUserMapper;
    @Resource
    private OrgTreeMapper orgTreeMapper;
    @Resource
    private RedisExtendService redisExtendService;
    @Resource
    private UserAuthConfig userAuthConfig;
    @Resource
    private TenantMapper tenantMapper;

    public void refreshUserPerms(RefreshUserPermVO vo) {
        Set<String> resp = new HashSet<>();
        Tenant tenant = tenantMapper.selectById(vo.getTenantId());
        if (tenant == null || tenant.getForbidden() > 0) {
            refreshAppPerms(vo.getUserId(), vo.getOrgId(), resp);
            return;
        }

        if (tenant.getAdminId().equals(vo.getUserId())) {
            //管理员
            List<TenantApp> tenantApps = tenantAppMapper.selectList(new QueryWrapper<TenantApp>()
                    .eq(TenantApp.COL_TENANT_ID, vo.getTenantId()));
            if (CollectionUtils.isEmpty(tenantApps)) {
                refreshAppPerms(vo.getUserId(), vo.getOrgId(), resp);
                return;
            }
            Set<String> grantAllAppIds = tenantApps.stream()
                    .filter(tenantApp -> tenantApp.getGrantAll() == 1)
                    .map(TenantApp::getAppId)
                    .collect(Collectors.toSet());
            if (!grantAllAppIds.isEmpty()) {
                resp.addAll(resourcePermMapper.listAppsPermIds(grantAllAppIds));
            }
            resp.addAll(tenantAppResourceMapper.getGrantedPermIds(vo.getTenantId(), ""));
            refreshAppPerms(vo.getUserId(), vo.getOrgId(), resp);
            return;
        }
        //普通用户
        OrgTree org = orgTreeMapper.selectById(vo.getOrgId());
        if (org == null) {
            refreshAppPerms(vo.getUserId(), vo.getOrgId(), resp);
            return;
        }
        resp.addAll(permUnitUserMapper.listUserPerms(vo.getUserId(), org.getIdPath()));
        refreshAppPerms(vo.getUserId(), vo.getOrgId(), resp);
    }

    /**
     * 刷新用户权限缓存
     *
     * @param userId  用户
     * @param permIds 权限id
     */
    private void refreshAppPerms(String userId, String orgId, Set<String> permIds) {
        String key = GatewayConstants.USER_PERM_CACHE_PREFIX.formatted(userId, orgId);
        redisExtendService.setAll(key, permIds, userAuthConfig.getPermExpire());
    }

    public List<ApiPermDTO> listAppPerms(String appId) {
        return resourcePermMapper.select4Gateway(appId);
    }

    public Map<String, String> getAllTenantAdmin() {
        List<Tenant> tenants = tenantMapper.selectList(new QueryWrapper<>());
        return tenants.stream()
                .filter(tenant -> tenant.getAdminId() != null)
                .collect(Collectors.toMap(Tenant::getId, Tenant::getAdminId));
    }
}
