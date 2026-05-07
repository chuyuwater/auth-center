package com.hbcy.authcenter.api.modules.minor.msg.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息模板 DTO（返回给前端）
 */
@Data
public class MsgTemplateDTO {
    private String id;
    private String templateNo;
    private String templateName;
    private String msgType;
    private String bizEntity;
    private String groupId;
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
