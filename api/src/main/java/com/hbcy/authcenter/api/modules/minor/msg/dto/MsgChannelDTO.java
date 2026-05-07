package com.hbcy.authcenter.api.modules.minor.msg.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息渠道 DTO（返回给前端）
 */
@Data
public class MsgChannelDTO {
    private String id;
    private String channelNo;
    private String channelName;
    private String channelType;
    private String provider;
    private String configJson;
    private String description;
    private Integer forbidden;
    private String tenantId;
    private String createUser;
    private String createUserName;
    private String updateUser;
    private String updateUserName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
