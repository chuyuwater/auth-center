package com.hbcy.authcenter.api.modules.minor.todo.controller;

import com.hbcy.authcenter.api.modules.minor.todo.dto.TodoDTO;
import com.hbcy.authcenter.api.modules.minor.todo.service.UserTodoService;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoQueryVO;
import com.hbcy.common.base.pojo.PageResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 待办管理
 * @author 姚泰然
 * @module msg
 * @date 2026-03-11 08:32
 */
@RestController
@RequestMapping("/api/v1/portal/v1/todo")
public class TodoOpController {
    @Resource
    private UserTodoService userTodoService;

    /**
     * 查询待办
     *
     * @param vo 查询条件
     * @return 待办分页结果
     */
    @GetMapping
    public PageResp<TodoDTO> queryTodo(UserTodoQueryVO vo) {
        return userTodoService.listTodo(vo);
    }

    /**
     * 获取已推送待办的应用列表（用于待办来源下拉多选）
     */
    @GetMapping("/source-apps")
    public List<Map<String, String>> listTodoSourceApps() {
        return userTodoService.listTodoSourceApps();
    }
}
