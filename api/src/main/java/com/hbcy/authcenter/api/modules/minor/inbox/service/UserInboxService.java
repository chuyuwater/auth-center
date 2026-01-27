package com.hbcy.authcenter.api.modules.minor.inbox.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.minor.inbox.dao.UserInboxMapper;
import com.hbcy.authcenter.api.modules.minor.inbox.dto.UserInboxDTO;
import com.hbcy.authcenter.api.modules.minor.inbox.model.UserInbox;
import com.hbcy.authcenter.api.modules.minor.inbox.vo.UserMsgBatchOpVO;
import com.hbcy.authcenter.api.modules.minor.inbox.vo.UserMsgCreateVO;
import com.hbcy.authcenter.api.modules.minor.inbox.vo.UserMsgQueryVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.db.model.PageRespEx;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-27 08:56
 */
@Service
public class UserInboxService extends ServiceImpl<UserInboxMapper, UserInbox> {
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
        List<UserInbox> msgs = new ArrayList<>();
        for (String targetUser : vo.getTargetUsers()) {
            UserInbox msg = new UserInbox()
                    .setId(UlidCreator.getUlid().toString())
                    .setSrcId(vo.getSrcId())
                    .setSrcApp(vo.getSrcApp())
                    .setMsgTitle(vo.getTitle())
                    .setMsgContent(vo.getContent())
                    .setTargetUser(targetUser)
                    .setSendTime(sendTime)
                    .setViewStatus(UserInbox.STATUS_UNREAD)
                    .setOriginJson(vo.getOriginJson())
                    .setMsgType(vo.getType());
            msgs.add(msg);
        }
        baseMapper.insertIgnore(msgs);
    }

    public PageResp<UserInboxDTO> queryMsg(UserMsgQueryVO vo) {
        Page<UserInboxDTO> dbPage = vo.getDbPage();
        vo.setUserId(UserContextUtils.getUserId());
        dbPage = baseMapper.query(dbPage, vo);
        return new PageRespEx<>(dbPage);
    }

    public void markAsRead(UserMsgBatchOpVO vo) {
        baseMapper.update(new UpdateWrapper<UserInbox>()
                .eq(UserInbox.COL_TARGET_USER, UserContextUtils.getUserId())
                .in(UserInbox.COL_ID, vo.getMsgIds())
                .set(UserInbox.COL_VIEW_STATUS, UserInbox.STATUS_READ));
    }

    public void batchDelete(UserMsgBatchOpVO vo) {
        baseMapper.delete(new UpdateWrapper<UserInbox>()
                .eq(UserInbox.COL_TARGET_USER, UserContextUtils.getUserId())
                .in(UserInbox.COL_ID, vo.getMsgIds()));
    }
}
