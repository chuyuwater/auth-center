package com.hbcy.authcenter.api.modules.core.user.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-26 08:45
 */
@Data
@NoArgsConstructor
@TableName(value = "user_org")
public class UserOrg {
    public static final String COL_ID = "id";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_ORG_ID = "org_id";
    public static final String COL_NODE_ID = "node_id";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_MAIN_JOB = "main_job";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private String userId;
    /**
     * 组织id
     */
    @TableField(value = "org_id")
    private String orgId;
    /**
     * 关联节点id
     */
    @TableField(value = "node_id")
    private String nodeId;
    /**
     * 租户id
     */
    @TableField(value = "tenant_id")
    private String tenantId;
    /**
     * 是否主职组织
     */
    @TableField(value = "main_job")
    private Integer mainJob;
    @TableField(value = "create_user")
    private String createUser;
    @TableField(value = "update_user")
    private String updateUser;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}