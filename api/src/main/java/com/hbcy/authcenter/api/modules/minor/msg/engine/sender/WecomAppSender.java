package com.hbcy.authcenter.api.modules.minor.msg.engine.sender;

import com.hbcy.authcenter.api.modules.minor.msg.engine.ChannelSender;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendContext;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 企微应用消息发送器
 * 当前为骨架实现
 */
@Component
@Slf4j
public class WecomAppSender implements ChannelSender {
    @Override
    public String getChannelType() {
        return "wechat";
    }

    @Override
    public String getProvider() {
        return "wecom_app";
    }

    @Override
    public MsgSendResult send(MsgSendContext context) {
        // TODO: 对接企微API
        log.warn("企微应用消息发送器尚未实现，跳过发送: userId={}", context.getTargetUserId());
        return MsgSendResult.fail("企微应用消息未实现");
    }
}
