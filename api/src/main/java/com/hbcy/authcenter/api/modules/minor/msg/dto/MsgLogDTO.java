package com.hbcy.authcenter.api.modules.minor.msg.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息日志 DTO（返回给前端）
 */
@Data
public class MsgLogDTO {
    private String id;
    private String msgTitle;
    private String msgContent;
    private String bizEntity;
    private String msgType;
    private String targetUser;
    private String targetUserName;
    private LocalDateTime receiveTime;
    private Integer viewStatus;
    private Integer sendStatus;
    private String failReason;
    private String jumpUrl;
    private String schemeId;
    private String schemeName;
    private String channelId;
    private String channelName;
    private Integer retryCount;
    private String originJson;
    private String srcApp;
    private String srcId;
    private String tenantId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
