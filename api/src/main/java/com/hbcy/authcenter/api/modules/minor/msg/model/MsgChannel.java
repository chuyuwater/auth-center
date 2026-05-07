package com.hbcy.authcenter.api.modules.minor.msg.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 消息渠道
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@TableName(value = "msg_channel")
@Accessors(chain = true)
public class MsgChannel extends BaseEntity {
    public static final String COL_ID = "id";
    public static final String COL_CHANNEL_NO = "channel_no";
    public static final String COL_CHANNEL_NAME = "channel_name";
    public static final String COL_CHANNEL_TYPE = "channel_type";
    public static final String COL_PROVIDER = "provider";
    public static final String COL_CONFIG_JSON = "config_json";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_FORBIDDEN = "forbidden";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_DELETE_TIME = "delete_time";

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @TableField(value = "channel_no")
    private String channelNo;
    @TableField(value = "channel_name")
    private String channelName;
    @TableField(value = "channel_type")
    private String channelType;
    @TableField(value = "provider")
    private String provider;
    @TableField(value = "config_json")
    private String configJson;
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
