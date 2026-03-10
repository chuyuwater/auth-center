package com.hbcy.authcenter.api.modules.core.app.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-24 16:50
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@TableName(value = "resource_perm")
public class ResourcePerm extends BaseEntity {
    public static final String COL_ID = "id";
    public static final String COL_APP_ID = "app_id";
    public static final String COL_RES_ID = "res_id";
    public static final String COL_PERM_NAME = "perm_name";
    public static final String COL_PERM_CODE = "perm_code";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 应用id，冗余方便搜索
     */
    @TableField(value = "app_id")
    private String appId;
    /**
     * 资源id
     */
    @TableField(value = "res_id")
    private String resId;
    /**
     * 权限名称
     */
    @TableField(value = "perm_name")
    private String permName;
    /**
     * 权限码
     */
    @TableField(value = "perm_code")
    private String permCode;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(value = "create_user")
    private String createUser;
    @TableField(value = "update_user")
    private String updateUser;
}