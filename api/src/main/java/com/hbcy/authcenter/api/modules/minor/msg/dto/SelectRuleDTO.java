package com.hbcy.authcenter.api.modules.minor.msg.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选人规则 DTO（返回给前端）
 */
@Data
public class SelectRuleDTO {
    private String id;
    private String ruleNo;
    private String ruleName;
    private String bizEntity;
    private String groupId;
    private Integer refCount;
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
