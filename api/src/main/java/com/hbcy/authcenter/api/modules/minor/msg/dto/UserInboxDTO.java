package com.hbcy.authcenter.api.modules.minor.msg.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2026-01-27 09:42
 */
@Data
public class UserInboxDTO {
    /**
     * 消息ID
     */
    private String id;

    /**
     * 消息来源应用
     */
    private String srcApp;

    /**
     * 消息标题
     */
    private String msgTitle;

    /**
     * 消息内容
     */
    private String msgContent;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 查看状态 (0-未查看, 1-已查看)
     */
    private int viewStatus;

    /**
     * 消息类型
     */
    private int msgType;

    /**
     * 关联链接
     */
    private String relateLink;
}
