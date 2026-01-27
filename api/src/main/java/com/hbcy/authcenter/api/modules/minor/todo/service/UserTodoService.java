package com.hbcy.authcenter.api.modules.minor.todo.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.minor.todo.dao.UserTodoMapper;
import com.hbcy.authcenter.api.modules.minor.todo.dto.UserTodoDTO;
import com.hbcy.authcenter.api.modules.minor.todo.model.UserTodo;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoBatchOpVO;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoCreateVO;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoQueryVO;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.db.model.PageRespEx;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-27
 */
@Service
public class UserTodoService extends ServiceImpl<UserTodoMapper, UserTodo> {

    @Transactional(rollbackFor = Exception.class)
    public void batchCreateTodo(UserTodoCreateVO vo) {
        LocalDateTime sendTime = vo.getCreateTime();
        if (sendTime == null) {
            sendTime = LocalDateTime.now();
        }
        List<UserTodo> todos = new ArrayList<>();
        for (String targetUser : vo.getTargetUsers()) {
            UserTodo todo = new UserTodo()
                    .setId(UlidCreator.getUlid().toString())
                    .setSrcId(vo.getSrcId())
                    .setSrcApp(vo.getSrcApp())
                    .setTodoTitle(vo.getTitle())
                    .setTodoContent(vo.getContent())
                    .setTargetUser(targetUser)
                    .setSendTime(sendTime)
                    .setProcessState(vo.getProcessState())
                    .setOriginJson(vo.getOriginJson())
                    .setTodoType(vo.getType())
                    .setRelateLink(vo.getLink());
            todos.add(todo);
        }
        baseMapper.insertIgnore(todos);
    }

    public PageResp<UserTodoDTO> queryTodo(UserTodoQueryVO vo) {
        Page<UserTodoDTO> dbPage = vo.getDbPage();
        vo.setUserId(UserContextUtils.getUserId());
        dbPage = baseMapper.query(dbPage, vo);
        return new PageRespEx<>(dbPage);
    }

    public void updateState(UserTodoUpdateVO vo) {
        baseMapper.update(new UpdateWrapper<UserTodo>()
                .eq(UserTodo.COL_TARGET_USER, vo.getUserId())
                .eq(UserTodo.COL_SRC_ID, vo.getSrcId())
                .eq(StringUtils.isNotBlank(vo.getSrcApp()), UserTodo.COL_SRC_APP, vo.getSrcApp())
                .set(UserTodo.COL_PROCESS_STATE, vo.getProcessState()));
    }

    public void batchDelete(UserTodoBatchOpVO vo) {
        baseMapper.delete(new UpdateWrapper<UserTodo>()
                .eq(UserTodo.COL_TARGET_USER, UserContextUtils.getUserId())
                .in(UserTodo.COL_ID, vo.getTodoIds()));
    }
}
