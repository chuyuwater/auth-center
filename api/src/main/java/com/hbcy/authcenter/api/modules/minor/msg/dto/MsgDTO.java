package com.hbcy.authcenter.api.modules.minor.msg.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2026-03-11 08:35
 */
@Data
public class MsgDTO {
    private String id;
    /**
     * 源消息id，用来去重
     */
    private String srcId;
    /**
     * 源应用id
     */
    private String srcApp;
    /**
     * 源应用名称
     */
    private String srcAppName;
    /**
     * 消息标题
     */
    private String msgTitle;
    /**
     * 消息内容
     */
    private String msgContent;
    /**
     * 接收人id
     */
    private String targetUser;

    /**
     * 接收人名称
     */
    private String targetUserName;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    /**
     * 0-未读，1-已读
     */
    private Integer viewStatus;
    /**
     * 0-普通消息，1-预警消息
     */
    private Integer msgType;
    /**
     * 跳转链接
     */
    private String relateLink;
    /**
     * 原始报文
     */
    private String originJson;
}
