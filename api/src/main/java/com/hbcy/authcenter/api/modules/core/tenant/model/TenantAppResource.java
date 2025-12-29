package com.hbcy.authcenter.api.modules.core.tenant.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-29 09:07
 */
@Data
@NoArgsConstructor
@TableName(value = "tenant_app_resource")
public class TenantAppResource {
    public static final String COL_ID = "id";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_APP_ID = "app_id";
    public static final String COL_PERM_ID = "perm_id";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 租户id
     */
    @TableField(value = "tenant_id")
    private String tenantId;
    /**
     * 应用id
     */
    @TableField(value = "app_id")
    private String appId;
    /**
     * 权限点id
     */
    @TableField(value = "perm_id")
    private String permId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(value = "create_user")
    private String createUser;
    @TableField(value = "update_user")
    private String updateUser;
}