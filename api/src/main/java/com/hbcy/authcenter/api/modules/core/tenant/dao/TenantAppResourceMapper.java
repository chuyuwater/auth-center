package com.hbcy.authcenter.api.modules.core.tenant.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantAppResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author 姚泰然
 * @date 2025-12-29 09:07
 */
@Mapper
public interface TenantAppResourceMapper extends BaseMapper<TenantAppResource> {
    /**
     * 获取授权应用资源权限ID列表
     *
     * @param tenantId 租户ID
     * @param appId    应用ID
     * @return 权限ID列表
     */
    Set<String> getGrantedPermIds(@Param("tenantId") String tenantId, @Param("appId") String appId);

    /**
     * 获取授权应用资源ID列表
     *
     * @param tenantId 租户ID
     * @param appId    应用ID
     * @return 资源ID列表
     */
    Set<String> getGrantedResIds(@Param("tenantId") String tenantId, @Param("appId") String appId);
}