package com.hbcy.authcenter.api.modules.minor.msg.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 消息日志
 */
@Data
@NoArgsConstructor
@TableName(value = "msg_log")
@Accessors(chain = true)
public class MsgLog {
    public static final String COL_ID = "id";
    public static final String COL_MSG_TITLE = "msg_title";
    public static final String COL_MSG_CONTENT = "msg_content";
    public static final String COL_BIZ_ENTITY = "biz_entity";
    public static final String COL_MSG_TYPE = "msg_type";
    public static final String COL_TARGET_USER = "target_user";
    public static final String COL_TARGET_USER_NAME = "target_user_name";
    public static final String COL_RECEIVE_TIME = "receive_time";
    public static final String COL_VIEW_STATUS = "view_status";
    public static final String COL_SEND_STATUS = "send_status";
    public static final String COL_FAIL_REASON = "fail_reason";
    public static final String COL_JUMP_URL = "jump_url";
    public static final String COL_SCHEME_ID = "scheme_id";
    public static final String COL_CHANNEL_ID = "channel_id";
    public static final String COL_RETRY_COUNT = "retry_count";
    public static final String COL_ORIGIN_JSON = "origin_json";
    public static final String COL_SRC_APP = "src_app";
    public static final String COL_SRC_ID = "src_id";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";

    public static final int SEND_STATUS_SENDING = 0;
    public static final int SEND_STATUS_SUCCESS = 1;
    public static final int SEND_STATUS_FAIL = 2;
    public static final int VIEW_STATUS_UNREAD = 0;
    public static final int VIEW_STATUS_READ = 1;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @TableField(value = "msg_title")
    private String msgTitle;
    @TableField(value = "msg_content")
    private String msgContent;
    @TableField(value = "biz_entity")
    private String bizEntity;
    @TableField(value = "msg_type")
    private String msgType;
    @TableField(value = "target_user")
    private String targetUser;
    @TableField(value = "target_user_name")
    private String targetUserName;
    @TableField(value = "receive_time")
    private LocalDateTime receiveTime;
    @TableField(value = "view_status")
    private Integer viewStatus;
    @TableField(value = "send_status")
    private Integer sendStatus;
    @TableField(value = "fail_reason")
    private String failReason;
    @TableField(value = "jump_url")
    private String jumpUrl;
    @TableField(value = "scheme_id")
    private String schemeId;
    @TableField(value = "channel_id")
    private String channelId;
    @TableField(value = "retry_count")
    private Integer retryCount;
    @TableField(value = "origin_json")
    private String originJson;
    @TableField(value = "src_app")
    private String srcApp;
    @TableField(value = "src_id")
    private String srcId;
    @TableField(value = "tenant_id")
    private String tenantId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
