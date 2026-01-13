package com.hbcy.authcenter.api.modules.core.perm.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-27 20:34
 */
@Data
@NoArgsConstructor
@TableName(value = "perm_unit_user")
public class PermUnitUser {
    public static final String COL_ID = "id";
    public static final String COL_UNIT_ID = "unit_id";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_ORG_ID = "org_id";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_FORBIDDEN = "forbidden";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @TableField(value = "unit_id")
    private String unitId;
    @TableField(value = "user_id")
    private String userId;
    /**
     * 主体域1：组织id
     */
    @TableField(value = "org_id")
    private String orgId;
    @TableField(value = "tenant_id")
    private String tenantId;
    @TableField(value = "forbidden")
    private Integer forbidden;
    @TableField(value = "create_user")
    private String createUser;
    @TableField(value = "update_user")
    private String updateUser;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}