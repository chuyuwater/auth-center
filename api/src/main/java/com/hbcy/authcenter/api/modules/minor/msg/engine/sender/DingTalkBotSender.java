package com.hbcy.authcenter.api.modules.minor.msg.engine.sender;

import com.hbcy.authcenter.api.modules.minor.msg.engine.ChannelSender;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendContext;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 钉钉群机器人发送器
 * 当前为骨架实现
 */
@Component
@Slf4j
public class DingTalkBotSender implements ChannelSender {
    @Override
    public String getChannelType() {
        return "dingtalk";
    }

    @Override
    public String getProvider() {
        return "dingtalk_bot";
    }

    @Override
    public MsgSendResult send(MsgSendContext context) {
        log.warn("钉钉群机器人发送器尚未实现，跳过发送");
        return MsgSendResult.fail("钉钉群机器人未实现");
    }
}
