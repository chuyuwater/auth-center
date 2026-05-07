package com.hbcy.authcenter.api.modules.minor.msg.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 发送方案
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@TableName(value = "msg_scheme")
@Accessors(chain = true)
public class MsgScheme extends BaseEntity {
    public static final String COL_ID = "id";
    public static final String COL_SCHEME_NO = "scheme_no";
    public static final String COL_SCHEME_NAME = "scheme_name";
    public static final String COL_TEMPLATE_ID = "template_id";
    public static final String COL_BIZ_TYPE = "biz_type";
    public static final String COL_GROUP_ID = "group_id";
    public static final String COL_RECEIVER_TYPE = "receiver_type";
    public static final String COL_RULE_ID = "rule_id";
    public static final String COL_RETRY_ENABLED = "retry_enabled";
    public static final String COL_RETRY_INTERVAL = "retry_interval";
    public static final String COL_RETRY_MAX_COUNT = "retry_max_count";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_FORBIDDEN = "forbidden";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_DELETE_TIME = "delete_time";

    public static final int RECEIVER_TYPE_DYNAMIC = 0;
    public static final int RECEIVER_TYPE_RULE = 1;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @TableField(value = "scheme_no")
    private String schemeNo;
    @TableField(value = "scheme_name")
    private String schemeName;
    @TableField(value = "template_id")
    private String templateId;
    @TableField(value = "biz_type")
    private String bizType;
    @TableField(value = "group_id")
    private String groupId;
    @TableField(value = "receiver_type")
    private Integer receiverType;
    @TableField(value = "rule_id")
    private String ruleId;
    @TableField(value = "retry_enabled")
    private Integer retryEnabled;
    @TableField(value = "retry_interval")
    private Integer retryInterval;
    @TableField(value = "retry_max_count")
    private Integer retryMaxCount;
    @TableField(value = "description")
    private String description;
    @TableField(value = "forbidden")
    private Integer forbidden;
    @TableField(value = "tenant_id")
    private String tenantId;
    @TableField(value = "create_user")
    private String createUser;
    @TableField(value = "update_user")
    private String updateUser;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(value = "delete_time")
    private Long deleteTime;
}
