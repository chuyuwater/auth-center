package com.hbcy.authcenter.api.modules.minor.msg.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.common.bean.NameCacheService;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.modules.core.app.model.App;
import com.hbcy.authcenter.api.modules.core.app.service.AppService;
import com.hbcy.authcenter.api.modules.core.inner.service.SDKService;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitUserService;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.dao.UserOrgMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.minor.msg.dao.UserMsgMapper;
import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgDTO;
import com.hbcy.authcenter.api.modules.minor.msg.dto.SourceAppDTO;
import com.hbcy.authcenter.api.modules.minor.msg.dto.UserMsgDTO;
import com.hbcy.authcenter.api.modules.minor.msg.model.UserMsg;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgBatchOpVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgCreateVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgQueryVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.base.util.BeanCopyUtils;
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
 * @date 2026-01-27 08:56
 */
@Service
public class UserMsgService extends ServiceImpl<UserMsgMapper, UserMsg> {
    public static final String PERM_QUERY_MSG = "msg:query";
    @Resource
    private UserMapper userMapper;
    @Resource
    private NameCacheService nameCacheService;
    @Resource
    private AppService appService;
    @Resource
    private SDKService sdkService;
    @Resource
    private PermUnitUserService permUnitUserService;
    @Resource
    private UserOrgMapper userOrgMapper;

    /**
     * NOTE: 创建消息和待办（以及更新待办）是portal平台侧的权限
     * 可以在平台侧创建一个用户，仅授予此权限，然后创建ak/sk给外部系统使用
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateMsg(UserMsgCreateVO vo) {
        LocalDateTime sendTime = vo.getCreateTime();
        if (sendTime == null) {
            sendTime = LocalDateTime.now();
        }
        List<UserMsg> msgs = new ArrayList<>();
        List<User> users = userMapper.selectByIds(vo.getTargetUsers());
        if (users.size() < vo.getTargetUsers().size()) {
            throw new ParamError("部分用户不存在");
        }
        Map<String, User> userMap = users.stream().collect(
                Collectors.toMap(User::getId, v -> v)
        );
        for (String targetUser : vo.getTargetUsers()) {
            UserMsg msg = new UserMsg()
                    .setId(UlidCreator.getUlid().toString())
                    .setSrcId(vo.getSrcId())
                    .setSrcApp(vo.getSrcApp())
                    .setMsgTitle(vo.getTitle())
                    .setMsgContent(vo.getContent())
                    .setTargetUser(targetUser)
                    .setSendTime(sendTime)
                    .setViewStatus(UserMsg.STATUS_UNREAD)
                    .setOriginJson(vo.getOriginJson())
                    .setMsgType(vo.getType())
                    .setTenantId(userMap.get(targetUser).getTenantId());
            msgs.add(msg);
        }
        baseMapper.insertIgnore(msgs);
    }

    public PageResp<UserMsgDTO> queryMsg(UserMsgQueryVO vo) {
        Page<UserMsgDTO> dbPage = vo.getDbPage();
        vo.setUserId(UserContextUtils.getUserId());
        dbPage = baseMapper.query4User(dbPage, vo);
        return new PageRespEx<>(dbPage);
    }

    public void markAsRead(UserMsgBatchOpVO vo) {
        baseMapper.update(new UpdateWrapper<UserMsg>()
                .eq(UserMsg.COL_TARGET_USER, UserContextUtils.getUserId())
                .in(UserMsg.COL_ID, vo.getMsgIds())
                .set(UserMsg.COL_VIEW_STATUS, UserMsg.STATUS_READ));
    }

    /**
     * 将当前用户全部未读消息标记为已读
     */
    public void markAllAsRead() {
        baseMapper.update(null, new UpdateWrapper<UserMsg>()
                .eq(UserMsg.COL_TARGET_USER, UserContextUtils.getUserId())
                .eq(UserMsg.COL_VIEW_STATUS, UserMsg.STATUS_UNREAD)
                .set(UserMsg.COL_VIEW_STATUS, UserMsg.STATUS_READ));
    }

    public void batchDelete(UserMsgBatchOpVO vo) {
        baseMapper.delete(new UpdateWrapper<UserMsg>()
                .eq(UserMsg.COL_TARGET_USER, UserContextUtils.getUserId())
                .in(UserMsg.COL_ID, vo.getMsgIds()));
    }

    /**
     * 查询当前用户有权查看的本下组织的所有用户的消息
     * @param vo 查询条件
     * @return 搜索结果分页
     */
    public PageResp<MsgDTO> listMsg(UserMsgQueryVO vo) {
        Page<MsgDTO> dbPage = vo.getDbPage();
        List<String> childOrgIds = sdkService.listGrantOrgs(
                UserContextUtils.getUserId(),
                G.APP_NAME,
                PERM_QUERY_MSG,
                UserContextUtils.getUserOrg()
        );
        if (childOrgIds.isEmpty()) {
            return new PageRespEx<>(dbPage);
        }
        vo.setSearchOrgIds(childOrgIds);
        Page<MsgDTO> page = baseMapper.listMsg(dbPage, vo);
        Set<String> appIds = new HashSet<>();
        Set<String> userIds = new HashSet<>();
        for (MsgDTO r : page.getRecords()) {
            appIds.add(r.getSrcApp());
            userIds.add(r.getTargetUser());
        }
        Map<String, String> appNameMap = appService.getNameMap(appIds);
        Map<String, String> userNameMap = nameCacheService.getUserNameMap(userIds);
        for (MsgDTO r : page.getRecords()) {
            r.setSrcAppName(appNameMap.get(r.getSrcApp()));
            r.setTargetUserName(userNameMap.get(r.getTargetUser()));
        }
        return new PageRespEx<>(page);
    }

    /**
     * 根据 id 查询消息详情
     */
    public MsgDTO getMsgById(String id) {
        UserMsg userMsg = baseMapper.selectById(id);
        if (userMsg == null) {
            return null;
        }
        String userId = UserContextUtils.getUserId();
        if (!userMsg.getTargetUser().equals(userId)) {
            //确认用户确实有该消息的访问权限
            Set<String> targetOrgs = userOrgMapper.listAllOrg(userMsg.getTargetUser());
            List<String> childOrgIds = sdkService.listGrantOrgs(
                    UserContextUtils.getUserId(),
                    G.APP_NAME,
                    PERM_QUERY_MSG,
                    UserContextUtils.getUserOrg()
            );
            if (!CollectionUtils.containsAny(new HashSet<>(childOrgIds), targetOrgs)) {
                throw new PermissionError();
            }
        }
        MsgDTO resp = BeanCopyUtils.copy(userMsg, MsgDTO.class);
        App app = appService.getById(resp.getSrcApp());
        resp.setSrcAppName(app == null ? resp.getSrcApp() : app.getNameCn());
        resp.setTargetUserName(nameCacheService.getUserName(resp.getTargetUser()));
        return resp;
    }

    /**
     * 查询已推送消息的应用列表（当前用户本下组织范围内）
     */
    public List<SourceAppDTO> listMsgSourceApps() {
        List<String> childOrgIds = sdkService.listGrantOrgs(
                UserContextUtils.getUserId(),
                G.APP_NAME,
                PERM_QUERY_MSG,
                UserContextUtils.getUserOrg()
        );
        if (childOrgIds == null || childOrgIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> appIds = baseMapper.listDistinctSrcAppInOrgs(childOrgIds);
        if (appIds == null || appIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, String> appNameMap = appService.getNameMap(new HashSet<>(appIds));
        return appIds.stream()
                .map(appId -> {
                    SourceAppDTO item = new SourceAppDTO();
                    item.setSrcApp(appId);
                    item.setSrcAppName(appNameMap.getOrDefault(appId, appId));
                    return item;
                })
                .collect(Collectors.toList());
    }
}
