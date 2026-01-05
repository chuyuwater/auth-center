package com.hbcy.authcenter.api.modules.core.tenant.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.app.dto.GrantAppDTO;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2025-12-23 17:19
 */
@Mapper
public interface TenantAppMapper extends BaseMapper<TenantApp> {
    /**
     * 查看租户已授权的应用列表（含禁用状态）
     *
     * @param tenantId 租户id
     * @return 应用列表
     */
    List<GrantAppDTO> listGrantApps(@Param("tenantId") String tenantId);

    /**
     * 切换应用禁用状态
     *
     * @param appId 应用id
     */
    void switchAppStatus(@Param("status") int status,
                         @Param("appId") String appId,
                         @Param("updateUser") String updateUser);

    /**
     * 切换租户禁用状态
     *
     * @param tenantId 租户id
     */
    void switchTenantStatus(@Param("status") int status,
                            @Param("tenantId") String tenantId,
                            @Param("updateUser") String updateUser);
}