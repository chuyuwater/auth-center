package com.hbcy.authcenter.api.modules.core.tenant.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.app.dto.AppCardDTO;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2025-12-23 17:19
 */
public interface TenantAppMapper extends BaseMapper<TenantApp> {
    List<AppCardDTO> listGrantApps(@Param("tenantId") String tenantId);
}