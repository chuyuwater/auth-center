package com.hbcy.authcenter.api.modules.minor.todo.controller;

import com.hbcy.authcenter.api.modules.minor.todo.dto.UserTodoDTO;
import com.hbcy.authcenter.api.modules.minor.todo.service.UserTodoService;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoBatchOpVO;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoQueryByMeVO;
import com.hbcy.common.base.pojo.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户待办
 *
 * @author 姚泰然
 * @module msg
 * @date 2026-01-27
 */
@RestController
@RequestMapping("api/portal/v1/user/todo")
public class UserTodoController {
    @Resource
    private UserTodoService userTodoService;

    /**
     * 查询待办列表
     *
     * @param vo 查询条件
     * @return 待办分页结果
     */
    @GetMapping
    public PageResp<UserTodoDTO> queryTodo(UserTodoQueryByMeVO vo) {
        return userTodoService.queryTodo(vo);
    }


    /**
     * 批量标记待办为已读
     *
     * @param vo 操作参数
     */
    @PostMapping("/mark-as-read")
    public void markAsRead(@Valid @RequestBody UserTodoBatchOpVO vo) {
        userTodoService.batchMarkAsRead(vo);
    }

    /**
     * 批量删除待办
     *
     * @param vo 操作参数
     */
    @PostMapping("/delete-batch")
    public void batchDelete(@Valid @RequestBody UserTodoBatchOpVO vo) {
        userTodoService.batchDelete(vo);
    }
}
