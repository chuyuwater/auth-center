package com.hbcy.authcenter.api.modules.minor.msg.engine;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 消息发送上下文
 */
@Data
@Accessors(chain = true)
public class MsgSendContext {
    private String channelId;
    private String channelName;
    private String channelType;
    private String provider;
    private String configJson;
    private String msgTitle;
    private String msgContent;
    private String targetUserId;
    private String targetUserName;
    /** 接收人手机号（短信渠道必填） */
    private String targetUserPhone;
    private String jumpUrl;
    /** 第三方平台模板ID（短信渠道必填） */
    private String thirdTemplateId;
    /** 模板变量 */
    private Map<String, String> variables;
}
