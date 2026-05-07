package com.hbcy.authcenter.api.modules.minor.msg.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 消息模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@TableName(value = "msg_template")
@Accessors(chain = true)
public class MsgTemplate extends BaseEntity {
    public static final String COL_ID = "id";
    public static final String COL_TEMPLATE_NO = "template_no";
    public static final String COL_TEMPLATE_NAME = "template_name";
    public static final String COL_MSG_TYPE = "msg_type";
    public static final String COL_BIZ_ENTITY = "biz_entity";
    public static final String COL_GROUP_ID = "group_id";
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
    @TableField(value = "template_no")
    private String templateNo;
    @TableField(value = "template_name")
    private String templateName;
    @TableField(value = "msg_type")
    private String msgType;
    @TableField(value = "biz_entity")
    private String bizEntity;
    @TableField(value = "group_id")
    private String groupId;
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
