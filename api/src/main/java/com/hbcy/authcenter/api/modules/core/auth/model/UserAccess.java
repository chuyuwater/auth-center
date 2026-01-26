package com.hbcy.authcenter.api.modules.core.auth.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2026-01-26 17:42
 */

/**
 * ak/sk通信
 */
@Data
@NoArgsConstructor
@TableName(value = "user_access")
public class UserAccess {
    public static final String COL_UPTIME_TIME = "uptime_time";
    public static final String COL_EXPIRE_DATE = "expire_date";
    public static final String COL_ID = "id";
    public static final String COL_KEY_NAME = "key_name";
    public static final String COL_ACCESS_KEY = "access_key";
    public static final String COL_SECRET_KEY = "secret_key";
    public static final String COL_FORBIDDEN = "forbidden";
    public static final String COL_EXPIRE_TIME = "expire_time";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 名称
     */
    @TableField(value = "key_name")
    private String keyName;
    @TableField(value = "access_key")
    private String accessKey;
    /**
     * 加密的密钥
     */
    @TableField(value = "secret_key")
    private String secretKey;
    /**
     * 是否禁用
     */
    @TableField(value = "forbidden")
    private Integer forbidden;
    /**
     * 过期时间
     */
    @TableField(value = "expire_time")
    private LocalDateTime expireTime;
    /**
     * ak归属用户
     */
    @TableField(value = "user_id")
    private String userId;
    /**
     * ak归属租户
     */
    @TableField(value = "tenant_id")
    private String tenantId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}