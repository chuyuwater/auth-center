package com.hbcy.authcenter.api.modules.minor.msg.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息模板内容 DTO（返回给前端）
 */
@Data
public class MsgTemplateContentDTO {
    private String id;
    private String templateId;
    private String msgTitle;
    private String msgContent;
    private String channelId;
    private String channelName;
    private String thirdTemplateId;
    private Integer showOrder;
    private String tenantId;
    private String createUser;
    private String createUserName;
    private String updateUser;
    private String updateUserName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
