package com.hbcy.authcenter.api.modules.minor.msg.engine.sender;

import com.hbcy.authcenter.api.modules.minor.msg.engine.ChannelSender;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendContext;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 站内信发送器
 * 直接写入 user_msg 表，不走外部HTTP
 */
@Component
@Slf4j
public class SiteMsgSender implements ChannelSender {
    @Override
    public String getChannelType() {
        return "site_msg";
    }

    @Override
    public String getProvider() {
        return "none";
    }

    @Override
    public boolean isSiteMsg() {
        return true;
    }

    @Override
    public MsgSendResult send(MsgSendContext context) {
        // 站内信的实际写入由 MsgSendEngine 统一处理（写入 user_msg 表）
        // 此处直接返回成功
        log.debug("站内信发送成功: userId={}, title={}", context.getTargetUserId(), context.getMsgTitle());
        return MsgSendResult.ok();
    }
}
