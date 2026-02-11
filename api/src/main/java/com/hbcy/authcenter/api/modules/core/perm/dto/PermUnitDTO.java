package com.hbcy.authcenter.api.modules.core.perm.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author 姚泰然
 * @date 2026-02-11 14:39
 */
@Data
public class PermUnitDTO {
    /**
     * 规则生成名称
     */
    private String id;
    /**
     * 名称
     */
    private String nameCn;
    /**
     * 授权模型，0-RBAC，1-ABAC
     */
    private Integer policyModel;
    /**
     * 归属分组
     */
    private String belongTo;
    /**
     * 分组名称
     */
    private String groupName;
    /**
     * 关联app名称
     */
    private Set<String> appNames;
    /**
     * 说明
     */
    private String memo;
    private Integer forbidden;
    private String tenantId;
    private String createUser;
    private String createUserName;
    private String updateUser;
    private String updateUserName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
