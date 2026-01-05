package com.hbcy.authcenter.api.modules.core.tenant.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 姚泰然
 * @date 2025-12-25 15:38
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
}