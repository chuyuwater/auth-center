package com.hbcy.authcenter.api.modules.minor.msg.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发送方案 DTO（返回给前端）
 */
@Data
public class MsgSchemeDTO {
    private String id;
    private String schemeNo;
    private String schemeName;
    private String templateId;
    private String templateName;
    private String bizType;
    private String groupId;
    private Integer receiverType;
    private String ruleId;
    private Integer retryEnabled;
    private Integer retryInterval;
    private Integer retryMaxCount;
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
