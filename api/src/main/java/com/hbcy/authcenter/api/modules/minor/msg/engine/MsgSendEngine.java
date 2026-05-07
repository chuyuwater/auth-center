package com.hbcy.authcenter.api.modules.minor.msg.engine;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgChannel;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgChannelMapper;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgLogMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgLog;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgScheme;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgSchemeChannel;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgSchemeChannelMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgTemplateContent;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgTemplateContentMapper;
import com.hbcy.authcenter.api.modules.minor.msg.engine.SchemeSendResultDTO.ChannelSendDetail;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.minor.msg.model.UserMsg;
import com.hbcy.authcenter.api.modules.minor.msg.dao.UserMsgMapper;
import com.hbcy.authcenter.api.modules.minor.msg.service.SelectRuleService;
import com.hbcy.common.base.error.ClientError;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 消息发送引擎
 * 核心流程：模板渲染 → 渠道路由 → 发送 → 记日志
 */
@Service
@Slf4j
public class MsgSendEngine {
    @Resource
    private ChannelSenderFactory channelSenderFactory;
    @Resource
    private MsgSchemeChannelMapper schemeChannelMapper;
    @Resource
    private MsgTemplateContentMapper templateContentMapper;
    @Resource
    private MsgChannelMapper channelMapper;
    @Resource
    private MsgLogMapper msgLogMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserMsgMapper userMsgMapper;
    @Resource(name = "msgSendExecutor")
    private Executor msgSendExecutor;
    @Resource
    private SelectRuleService selectRuleService;

    /**
     * 通过方案发送消息（同步）
     * 调用方可通过 @Async 或线程池异步执行
     */
    public SchemeSendResultDTO sendByScheme(MsgScheme scheme, Set<String> targetUsers,
                                            Map<String, String> variables, String jumpUrl, String originJson) {
        if (scheme.getForbidden() == 1) {
            throw new ClientError("方案已禁用");
        }

        // 1. 解析接收人
        Set<String> users = targetUsers;
        if (scheme.getReceiverType() == MsgScheme.RECEIVER_TYPE_RULE
                && !scheme.getRuleId().isBlank()) {
            users = selectRuleService.computeTargetUsers(scheme.getRuleId());
        }
        if (CollectionUtils.isEmpty(users)) {
            SchemeSendResultDTO result = new SchemeSendResultDTO();
            result.setTotalCount(0);
            result.setSuccessCount(0);
            result.setFailCount(0);
            result.setDetails(List.of());
            return result;
        }

        // 2. 获取方案绑定的渠道
        List<MsgSchemeChannel> schemeChannels = schemeChannelMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MsgSchemeChannel>()
                        .eq(MsgSchemeChannel.COL_SCHEME_ID, scheme.getId()));
        List<String> channelIds = schemeChannels.stream()
                .map(MsgSchemeChannel::getChannelId).collect(Collectors.toList());

        // 3. 获取模板内容（按渠道分组）
        List<MsgTemplateContent> contents = templateContentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MsgTemplateContent>()
                        .eq(MsgTemplateContent.COL_TEMPLATE_ID, scheme.getTemplateId())
                        .eq(MsgTemplateContent.COL_DELETE_TIME, 0)
                        .in(MsgTemplateContent.COL_CHANNEL_ID, channelIds));

        // 4. 获取渠道信息
        List<MsgChannel> channels = channelMapper.selectBatchIds(channelIds);
        Map<String, MsgChannel> channelMap = channels.stream()
                .collect(Collectors.toMap(MsgChannel::getId, c -> c));

        // 5. 获取用户信息
        List<User> userList = userMapper.selectByIds(users);
        Map<String, User> userMap = userList.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 6. 遍历用户+渠道发送
        int successCount = 0;
        int failCount = 0;
        List<ChannelSendDetail> details = Collections.synchronizedList(new ArrayList<>());

        for (MsgTemplateContent content : contents) {
            MsgChannel channel = channelMap.get(content.getChannelId());
            if (channel == null || channel.getForbidden() == 1) continue;

            ChannelSender sender = channelSenderFactory.getSender(channel.getChannelType(), channel.getProvider());
            if (sender == null) {
                log.warn("未找到渠道发送器: {}/{}", channel.getChannelType(), channel.getProvider());
                continue;
            }

            int channelSuccess = 0;
            int channelFail = 0;

            for (String userId : users) {
                User user = userMap.get(userId);
                if (user == null) continue;

                // 构建发送上下文
                MsgSendContext ctx = new MsgSendContext()
                        .setChannelId(channel.getId())
                        .setChannelName(channel.getChannelName())
                        .setChannelType(channel.getChannelType())
                        .setProvider(channel.getProvider())
                        .setConfigJson(channel.getConfigJson())
                        .setMsgTitle(TemplateRenderer.render(content.getMsgTitle(), variables))
                        .setMsgContent(TemplateRenderer.render(content.getMsgContent(), variables))
                        .setTargetUserId(userId)
                        .setTargetUserName(user.getRealName())
                        .setTargetUserPhone(user.getPhone())
                        .setJumpUrl(jumpUrl)
                        .setThirdTemplateId(content.getThirdTemplateId())
                        .setVariables(variables);

                // 发送消息
                MsgSendResult sendResult;
                try {
                    sendResult = sender.send(ctx);
                } catch (Exception e) {
                    log.error("消息发送异常: channel={}, userId={}", channel.getId(), userId, e);
                    sendResult = MsgSendResult.fail(e.getMessage());
                }

                // 写入消息日志
                MsgLog msgLog = new MsgLog()
                        .setId(UlidCreator.getUlid().toString())
                        .setMsgTitle(ctx.getMsgTitle())
                        .setMsgContent(ctx.getMsgContent())
                        .setBizEntity(scheme.getBizType())
                        .setMsgType(scheme.getBizType())
                        .setTargetUser(userId)
                        .setTargetUserName(user.getRealName())
                        .setReceiveTime(LocalDateTime.now())
                        .setViewStatus(MsgLog.VIEW_STATUS_UNREAD)
                        .setSendStatus(sendResult.isSuccess() ? MsgLog.SEND_STATUS_SUCCESS : MsgLog.SEND_STATUS_FAIL)
                        .setFailReason(sendResult.getFailReason() != null ? sendResult.getFailReason() : "")
                        .setJumpUrl(jumpUrl != null ? jumpUrl : "")
                        .setSchemeId(scheme.getId())
                        .setChannelId(channel.getId())
                        .setRetryCount(0)
                        .setOriginJson(originJson)
                        .setTenantId(scheme.getTenantId());
                msgLogMapper.insert(msgLog);

                // 站内信双写 user_msg
                if (sender.isSiteMsg() && sendResult.isSuccess()) {
                    UserMsg userMsg = new UserMsg()
                            .setId(UlidCreator.getUlid().toString())
                            .setSrcApp("portal")
                            .setSrcId(scheme.getSchemeNo())
                            .setMsgTitle(ctx.getMsgTitle())
                            .setMsgContent(ctx.getMsgContent())
                            .setTargetUser(userId)
                            .setSendTime(LocalDateTime.now())
                            .setViewStatus(UserMsg.STATUS_UNREAD)
                            .setMsgType(mapMsgType(scheme.getBizType()))
                            .setRelateLink(jumpUrl != null ? jumpUrl : "")
                            .setOriginJson(originJson)
                            .setTenantId(scheme.getTenantId());
                    userMsgMapper.insert(userMsg);
                }

                if (sendResult.isSuccess()) {
                    channelSuccess++;
                } else {
                    channelFail++;
                }
            }

            successCount += channelSuccess;
            failCount += channelFail;
            details.add(new ChannelSendDetail(channel.getId(), channel.getChannelName(),
                    channelFail > 0 ? 2 : 1, channelFail > 0 ? "部分失败" : ""));
        }

        SchemeSendResultDTO result = new SchemeSendResultDTO();
        result.setTotalCount(users.size() * contents.size());
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setDetails(details);
        return result;
    }

    /**
     * 重试发送
     */
    public void retrySend(MsgLog msgLog) {
        MsgChannel channel = channelMapper.selectById(msgLog.getChannelId());
        if (channel == null) return;

        ChannelSender sender = channelSenderFactory.getSender(channel.getChannelType(), channel.getProvider());
        if (sender == null) return;

        MsgSendContext ctx = new MsgSendContext()
                .setChannelId(channel.getId())
                .setChannelName(channel.getChannelName())
                .setChannelType(channel.getChannelType())
                .setProvider(channel.getProvider())
                .setConfigJson(channel.getConfigJson())
                .setMsgTitle(msgLog.getMsgTitle())
                .setMsgContent(msgLog.getMsgContent())
                .setTargetUserId(msgLog.getTargetUser())
                .setTargetUserName(msgLog.getTargetUserName())
                .setJumpUrl(msgLog.getJumpUrl())
                .setSchemeId(msgLog.getSchemeId())
                .setChannelId(msgLog.getChannelId());

        MsgSendResult sendResult;
        try {
            sendResult = sender.send(ctx);
        } catch (Exception e) {
            log.error("消息重试发送异常: logId={}", msgLog.getId(), e);
            sendResult = MsgSendResult.fail(e.getMessage());
        }

        int newRetryCount = msgLog.getRetryCount() + 1;
        if (sendResult.isSuccess()) {
            msgLogMapper.update(new UpdateWrapper<MsgLog>()
                    .eq(MsgLog.COL_ID, msgLog.getId())
                    .set(MsgLog.COL_SEND_STATUS, MsgLog.SEND_STATUS_SUCCESS)
                    .set(MsgLog.COL_RETRY_COUNT, newRetryCount));
        } else {
            msgLogMapper.update(new UpdateWrapper<MsgLog>()
                    .eq(MsgLog.COL_ID, msgLog.getId())
                    .set(MsgLog.COL_RETRY_COUNT, newRetryCount)
                    .set(MsgLog.COL_FAIL_REASON, sendResult.getFailReason()));
        }
    }

    /**
     * 新msg_type到旧msg_type的映射（user_msg兼容）
     */
    private int mapMsgType(String msgType) {
        return switch (msgType) {
            case "warning", "alarm" -> 1;
            default -> 0;
        };
    }
}
