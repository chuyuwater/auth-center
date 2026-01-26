package com.hbcy.authcenter.api.modules.core.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.auth.model.UserAccess;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 姚泰然
 * @date 2026-01-26 17:42
 */
@Mapper
public interface UserAccessMapper extends BaseMapper<UserAccess> {
}