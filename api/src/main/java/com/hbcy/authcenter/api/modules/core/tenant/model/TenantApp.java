package com.hbcy.authcenter.api.modules.core.tenant.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-23 17:19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@TableName(value = "tenant_app")
public class TenantApp extends BaseEntity {
    public static final String COL_ID = "id";
    public static final String COL_APP_ID = "app_id";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_ORG_TREE = "org_tree";
    public static final String COL_FORBIDDEN = "forbidden";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_DELETE_TIME = "delete_time";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    public static final String COL_GRANT_ALL = "grant_all";

    /**
     * ulid
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 应用id
     */
    @TableField(value = "app_id")
    private String appId;
    /**
     * 租户id
     */
    @TableField(value = "tenant_id")
    private String tenantId;
    /**
     * 是否全部授权
     */
    private Integer grantAll;
    /**
     * 组织树id
     */
    @TableField(value = "org_tree")
    private String orgTree;
    @TableField(value = "forbidden")
    private Integer forbidden;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(value = "delete_time")
    private Long deleteTime;
    @TableField(value = "create_user")
    private String createUser;
    @TableField(value = "update_user")
    private String updateUser;
}