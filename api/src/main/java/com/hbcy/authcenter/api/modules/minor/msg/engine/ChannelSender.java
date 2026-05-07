package com.hbcy.authcenter.api.modules.minor.msg.engine;

/**
 * 渠道发送接口
 */
public interface ChannelSender {
    /**
     * 支持的渠道类型
     */
    String getChannelType();

    /**
     * 服务商编码
     */
    String getProvider();

    /**
     * 发送消息
     */
    MsgSendResult send(MsgSendContext context);

    /**
     * 是否为站内信（特殊处理：写入user_msg）
     */
    default boolean isSiteMsg() {
        return false;
    }
}
