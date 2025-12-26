package com.hbcy.authcenter.api.modules.core.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.common.web.api.NamedId;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author 姚泰然
 * @date 2025-12-26 08:54
 */
public interface UserMapper extends BaseMapper<User> {
    List<NamedId> selectNameByIds(@Param("uids") Set<String> uids);

    String selectNameById(@Param("userId") String userId);
}