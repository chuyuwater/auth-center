package com.hbcy.authcenter.api.modules.minor.msg.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 消息模板内容
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@TableName(value = "msg_template_content")
@Accessors(chain = true)
public class MsgTemplateContent extends BaseEntity {
    public static final String COL_ID = "id";
    public static final String COL_TEMPLATE_ID = "template_id";
    public static final String COL_MSG_TITLE = "msg_title";
    public static final String COL_MSG_CONTENT = "msg_content";
    public static final String COL_CHANNEL_ID = "channel_id";
    public static final String COL_THIRD_TEMPLATE_ID = "third_template_id";
    public static final String COL_SHOW_ORDER = "show_order";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_DELETE_TIME = "delete_time";

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @TableField(value = "template_id")
    private String templateId;
    @TableField(value = "msg_title")
    private String msgTitle;
    @TableField(value = "msg_content")
    private String msgContent;
    @TableField(value = "channel_id")
    private String channelId;
    @TableField(value = "third_template_id")
    private String thirdTemplateId;
    @TableField(value = "show_order")
    private Integer showOrder;
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
