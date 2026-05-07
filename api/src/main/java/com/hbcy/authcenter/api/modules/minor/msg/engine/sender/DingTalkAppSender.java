package com.hbcy.authcenter.api.modules.minor.msg.engine.sender;

import com.hbcy.authcenter.api.modules.minor.msg.engine.ChannelSender;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendContext;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 钉钉应用消息发送器
 * 当前为骨架实现
 */
@Component
@Slf4j
public class DingTalkAppSender implements ChannelSender {
    @Override
    public String getChannelType() {
        return "dingtalk";
    }

    @Override
    public String getProvider() {
        return "dingtalk_app";
    }

    @Override
    public MsgSendResult send(MsgSendContext context) {
        log.warn("钉钉应用消息发送器尚未实现，跳过发送: userId={}", context.getTargetUserId());
        return MsgSendResult.fail("钉钉应用消息未实现");
    }
}
