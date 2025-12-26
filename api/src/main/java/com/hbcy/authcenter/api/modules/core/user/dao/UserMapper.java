package com.hbcy.authcenter.api.modules.core.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import org.apache.ibatis.annotations.Param;

/**
 * @author 姚泰然
 * @date 2025-12-26 08:54
 */
public interface UserMapper extends BaseMapper<User> {
    String selectNameById(@Param("userId") String userId);
}