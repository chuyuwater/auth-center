package com.hbcy.authcenter.api.modules.minor.msg.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.minor.msg.dao.UserMsgMapper;
import com.hbcy.authcenter.api.modules.minor.msg.dto.UserMsgDTO;
import com.hbcy.authcenter.api.modules.minor.msg.model.UserMsg;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgBatchOpVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgCreateVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgQueryVO;
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
 * @date 2026-01-27 08:56
 */
@Service
public class UserMsgService extends ServiceImpl<UserMsgMapper, UserMsg> {
    @Resource
    private UserMapper userMapper;

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
                    .setSrcUser(vo.getSrcUser())
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
        dbPage = baseMapper.query(dbPage, vo);
        return new PageRespEx<>(dbPage);
    }

    public void markAsRead(UserMsgBatchOpVO vo) {
        baseMapper.update(new UpdateWrapper<UserMsg>()
                .eq(UserMsg.COL_TARGET_USER, UserContextUtils.getUserId())
                .in(UserMsg.COL_ID, vo.getMsgIds())
                .set(UserMsg.COL_VIEW_STATUS, UserMsg.STATUS_READ));
    }

    public void batchDelete(UserMsgBatchOpVO vo) {
        baseMapper.delete(new UpdateWrapper<UserMsg>()
                .eq(UserMsg.COL_TARGET_USER, UserContextUtils.getUserId())
                .in(UserMsg.COL_ID, vo.getMsgIds()));
    }
}
