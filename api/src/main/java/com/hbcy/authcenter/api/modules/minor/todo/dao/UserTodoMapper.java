package com.hbcy.authcenter.api.modules.minor.todo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbcy.authcenter.api.modules.minor.todo.dto.TodoDTO;
import com.hbcy.authcenter.api.modules.minor.todo.dto.UserTodoDTO;
import com.hbcy.authcenter.api.modules.minor.todo.model.UserTodo;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoQueryByMeVO;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoQueryByOpVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-02-06 17:41
 */
@Mapper
public interface UserTodoMapper extends BaseMapper<UserTodo> {
    void insertIgnore(@Param("todos") List<UserTodo> todos);

    Page<UserTodoDTO> query4user(Page<?> dbPage, @Param("vo") UserTodoQueryByMeVO vo);

    Page<TodoDTO> listTodo(Page<?> dbPage, @Param("vo") UserTodoQueryByOpVO vo);

    /**
     * 查询本下组织范围内有推送过待办的应用 id 列表（去重）
     */
    List<String> listDistinctSrcApp(@Param("orgIds") Collection<String> orgIds);
}