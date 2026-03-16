package com.hbcy.authcenter.api.modules.minor.todo.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.google.common.base.Preconditions;
import com.hbcy.authcenter.api.common.enums.TodoQueryScopeEnum;
import com.hbcy.authcenter.api.common.bean.NameCacheService;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.modules.core.app.dto.GrantAppDTO;
import com.hbcy.authcenter.api.modules.core.app.service.AppService;
import com.hbcy.authcenter.api.modules.core.inner.service.SDKService;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitUserService;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.minor.msg.dto.SourceAppDTO;
import com.hbcy.authcenter.api.modules.minor.msg.dto.UserMsgDTO;
import com.hbcy.authcenter.api.modules.minor.todo.dao.UserTodoMapper;
import com.hbcy.authcenter.api.modules.minor.todo.dto.TodoDTO;
import com.hbcy.authcenter.api.modules.minor.todo.dto.UserTodoDTO;
import com.hbcy.authcenter.api.modules.minor.todo.model.UserTodo;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoBatchOpVO;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoQueryByMeVO;
import com.hbcy.authcenter.api.modules.minor.todo.vo.UserTodoQueryByOpVO;
import com.hbcy.authcenter.api.modules.sys.dict.service.DictEnumAdapter;
import com.hbcy.authcenter.sdk.feign.vo.TodoCreateVO;
import com.hbcy.authcenter.sdk.feign.vo.TodoUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.db.model.PageRespEx;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 姚泰然
 * @date 2026-01-27
 */
@Service
public class UserTodoService extends ServiceImpl<UserTodoMapper, UserTodo> {
    public static final String PERM_VIEW_TODO = "todo:query";
    public static final String PROCESS_STATE = "TODO_PROCESS_STATE";
    @Resource
    private UserMapper userMapper;
    @Resource
    private NameCacheService nameCacheService;
    @Resource
    private AppService appService;
    @Resource
    private SDKService sdkService;
    @Resource
    private DictEnumAdapter dictEnumAdapter;
    @Resource
    private PermUnitUserService permUnitUserService;

    @Transactional(rollbackFor = Exception.class)
    public void batchCreateTodo(TodoCreateVO vo) {
        LocalDateTime sendTime = vo.getCreateTime();
        if (sendTime == null) {
            sendTime = LocalDateTime.now();
        }
        List<User> users = userMapper.selectByIds(vo.getTargetUsers());
        if (users.size() < vo.getTargetUsers().size()) {
            throw new ParamError("部分用户不存在");
        }
        if (!dictEnumAdapter.isValidValue(PROCESS_STATE, vo.getProcessState().toString())) {
            throw new ParamError("处理状态无效");
        }
        Map<String, User> userMap = users.stream().collect(
                Collectors.toMap(User::getId, v -> v));
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
                    .setViewState(0)
                    .setProcessState(vo.getProcessState())
                    .setOriginJson(vo.getOriginJson())
                    .setTodoType(vo.getType())
                    .setRelateLink(vo.getLink())
                    .setTenantId(userMap.get(targetUser).getTenantId())
                    .setInitiatorId(vo.getInitiatorId())
                    .setUrgeFlag(0);
            todos.add(todo);
        }
        baseMapper.insertIgnore(todos);
    }

    /** 待办列表允许的排序字段（与 PageVO orderBy 规则一致：以 "-" 开头为倒序，多列用逗号分隔） */
    private static final Set<String> TODO_ORDER_ALLOWED = Set.of(
            "create_time", "-create_time", "send_time", "-send_time", "urge_flag", "-urge_flag",
            "-urge_flag,create_time"
    );

    public PageResp<UserTodoDTO> queryTodo(UserTodoQueryByMeVO vo) {
        Preconditions.checkNotNull(vo.getScope(), "查询范围不能为空");
        Page<UserTodoDTO> dbPage = vo.getOrderedDbPage(Map.of("", TODO_ORDER_ALLOWED));
        switch (vo.getScope()) {
            case TARGET_ME -> vo.setUserId(UserContextUtils.getUserId());
            case INITIATOR_ME -> vo.setInitiatorId(UserContextUtils.getUserId());
        }
        dbPage = baseMapper.query4user(dbPage, vo);
        Set<String> appIds = dbPage.getRecords().stream().map(
                UserTodoDTO::getSrcApp).collect(Collectors.toSet());
        Map<String, String> nameMap = appService.getNameMap(appIds);
        for (UserTodoDTO r : dbPage.getRecords()) {
            r.setSrcAppName(nameMap.getOrDefault(r.getSrcApp(), r.getSrcApp()));
        }
        return new PageRespEx<>(dbPage);
    }

    public void updateState(TodoUpdateVO vo) {
        if (!dictEnumAdapter.isValidValue(PROCESS_STATE, vo.getProcessState().toString())) {
            throw new ParamError("处理状态无效");
        }
        baseMapper.update(new UpdateWrapper<UserTodo>()
                .in(UserTodo.COL_TARGET_USER, vo.getUserIds())
                .eq(UserTodo.COL_SRC_ID, vo.getSrcId())
                .eq(UserTodo.COL_SRC_APP, vo.getSrcApp())
                .set(UserTodo.COL_PROCESS_STATE, vo.getProcessState())
                .set(UserTodo.COL_URGE_FLAG, vo.getUrgeFlag()));
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

    /**
     * 查询当前用户有权查看的本下组织的所有用户的待办
     *
     * @param vo 查询条件
     * @return 搜索结果分页
     */
    public PageResp<TodoDTO> listTodo(UserTodoQueryByOpVO vo) {
        Page<TodoDTO> dbPage = vo.getDbPage();
        if (!UserContextUtils.isTenantAdmin()) {
            List<String> childOrgIds = sdkService.listGrantOrgs(
                    UserContextUtils.getUserId(),
                    G.APP_NAME,
                    PERM_VIEW_TODO,
                    UserContextUtils.getUserOrg()
            );
            vo.setSearchOrgIds(childOrgIds);
        }
        vo.setTenantId(UserContextUtils.getTenantId());
        Page<TodoDTO> page = baseMapper.listTodo(dbPage, vo);
        Set<String> appIds = new HashSet<>();
        Set<String> userIds = new HashSet<>();
        for (TodoDTO r : page.getRecords()) {
            appIds.add(r.getSrcApp());
            userIds.add(r.getTargetUser());
        }
        Map<String, String> appNameMap = appService.getNameMap(appIds);
        Map<String, String> userNameMap = nameCacheService.getUserNameMap(userIds);
        for (TodoDTO r : page.getRecords()) {
            r.setSrcAppName(appNameMap.get(r.getSrcApp()));
            r.setTargetUserName(userNameMap.get(r.getTargetUser()));
        }
        return new PageRespEx<>(page);
    }

    /**
     * 查询当前用户本下组织范围内已推送待办的应用列表（用于待办来源下拉）
     */
    public List<SourceAppDTO> listTodoSourceApps() {
        List<GrantAppDTO> grantApps = permUnitUserService.listApp(
                UserContextUtils.getUserOrg(), false);
        if (CollectionUtils.isEmpty(grantApps)) {
            return List.of();
        }
        return grantApps.stream()
                .map(app -> {
                    SourceAppDTO item = new SourceAppDTO();
                    item.setSrcApp(app.getAppId());
                    item.setSrcAppName(app.getNameCn());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
