package com.hbcy.authcenter.api.modules.minor.todo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.minor.todo.model.UserTodo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 姚泰然
 * @date 2026-01-27 10:18
 */
@Mapper
public interface UserTodoMapper extends BaseMapper<UserTodo> {
}