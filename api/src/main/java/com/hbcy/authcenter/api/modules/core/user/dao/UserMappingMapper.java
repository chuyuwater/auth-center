package com.hbcy.authcenter.api.modules.core.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.user.model.UserMapping;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 姚泰然
 * @date 2026-02-11 16:16
 */
@Mapper
public interface UserMappingMapper extends BaseMapper<UserMapping> {
}