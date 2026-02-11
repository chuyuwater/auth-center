package com.hbcy.authcenter.api.modules.minor.msg.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/** 站内信
 * @author 姚泰然
 * @date 2026-02-10 11:04
 */

@Data
@NoArgsConstructor
@TableName(value = "user_msg")
@Accessors(chain = true)
public class UserMsg {
    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ = 1;
    public static final String COL_ID = "id";
    public static final String COL_SRC_ID = "src_id";
    public static final String COL_SRC_APP = "src_app";
    public static final String COL_SRC_USER = "src_user";
    public static final String COL_MSG_TITLE = "msg_title";
    public static final String COL_MSG_CONTENT = "msg_content";
    public static final String COL_TARGET_USER = "target_user";
    public static final String COL_SEND_TIME = "send_time";
    public static final String COL_VIEW_STATUS = "view_status";
    public static final String COL_MSG_TYPE = "msg_type";
    public static final String COL_RELATE_LINK = "relate_link";
    public static final String COL_ORIGIN_JSON = "origin_json";
    public static final String COL_CREATE_TIME = "create_time";
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 源消息id，用来去重
     */
    @TableField(value = "src_id")
    private String srcId;
    /**
     * 源应用id
     */
    @TableField(value = "src_app")
    private String srcApp;
    /**
     * 源系统用户标识
     */
    @TableField(value = "src_user")
    private String srcUser;
    /**
     * 消息标题
     */
    @TableField(value = "msg_title")
    private String msgTitle;
    /**
     * 消息内容
     */
    @TableField(value = "msg_content")
    private String msgContent;
    /**
     * 目标用户
     */
    @TableField(value = "target_user")
    private String targetUser;
    /**
     * 发送时间
     */
    @TableField(value = "send_time")
    private LocalDateTime sendTime;
    /**
     * 0-未读，1-已读
     */
    @TableField(value = "view_status")
    private Integer viewStatus;
    /**
     * 0-普通消息，1-预警消息
     */
    @TableField(value = "msg_type")
    private Integer msgType;
    /**
     * 跳转链接
     */
    @TableField(value = "relate_link")
    private String relateLink;
    /**
     * 原始报文
     */
    @TableField(value = "origin_json")
    private String originJson;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}