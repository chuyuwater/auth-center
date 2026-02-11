package com.hbcy.authcenter.api.modules.minor.todo.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.minor.todo.dao.UserTodoMapper;
import com.hbcy.authcenter.api.modules.minor.todo.dto.UserTodoDTO;
import com.hbcy.authcenter.api.modules.minor.todo.model.UserTodo;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoBatchOpVO;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoCreateVO;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoQueryVO;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.db.model.PageRespEx;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 姚泰然
 * @date 2026-01-27
 */
@Service
public class UserTodoService extends ServiceImpl<UserTodoMapper, UserTodo> {
    @Resource
    private UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public void batchCreateTodo(UserTodoCreateVO vo) {
        LocalDateTime sendTime = vo.getCreateTime();
        if (sendTime == null) {
            sendTime = LocalDateTime.now();
        }
        List<User> users = userMapper.selectByIds(vo.getTargetUsers());
        if (users.size() < vo.getTargetUsers().size()) {
            throw new ParamError("部分用户不存在");
        }
        Map<String, User> userMap = users.stream().collect(
                Collectors.toMap(User::getId, v -> v)
        );
        List<UserTodo> todos = new ArrayList<>();
        for (String targetUser : vo.getTargetUsers()) {
            UserTodo todo = new UserTodo()
                    .setId(UlidCreator.getUlid().toString())
                    .setSrcId(vo.getSrcId())
                    .setSrcUser(vo.getSrcUser())
                    .setSrcApp(vo.getSrcApp())
                    .setTodoTitle(vo.getTitle())
                    .setTodoContent(vo.getContent())
                    .setTargetUser(targetUser)
                    .setSendTime(sendTime)
                    .setViewState(0)
                    .setProcessState(vo.getProcessState())
                    .setOriginJson(vo.getOriginJson())
                    .setTodoType(vo.getType())
                    .setRelateLink(vo.getLink())
                    .setTenantId(userMap.get(targetUser).getTenantId());
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
                .in(UserTodo.COL_TARGET_USER, vo.getUserIds())
                .eq(UserTodo.COL_SRC_ID, vo.getSrcId())
                .eq(UserTodo.COL_SRC_APP, vo.getSrcApp())
                .set(UserTodo.COL_PROCESS_STATE, vo.getProcessState()));
    }

    public void batchDelete(UserTodoBatchOpVO vo) {
        baseMapper.delete(new UpdateWrapper<UserTodo>()
                .eq(UserTodo.COL_TARGET_USER, UserContextUtils.getUserId())
                .in(UserTodo.COL_ID, vo.getTodoIds()));
    }

    public void batchMarkAsRead(UserTodoBatchOpVO vo) {
        baseMapper.update(new UpdateWrapper<UserTodo>()
                .eq(UserTodo.COL_TARGET_USER, UserContextUtils.getUserId())
                .in(UserTodo.COL_ID, vo.getTodoIds())
                .set(UserTodo.COL_VIEW_STATE, 1));
    }
}
