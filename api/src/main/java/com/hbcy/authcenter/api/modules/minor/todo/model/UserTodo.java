package com.hbcy.authcenter.api.modules.minor.todo.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2026-01-27 11:18
 */
@Data
@NoArgsConstructor
@TableName(value = "user_todo")
@Accessors(chain = true)
public class UserTodo {
    public static final String COL_ID = "id";
    public static final String COL_SRC_APP = "src_app";
    public static final String COL_SRC_ID = "src_id";
    public static final String COL_TODO_TITLE = "todo_title";
    public static final String COL_TODO_CONTENT = "todo_content";
    public static final String COL_TARGET_USER = "target_user";
    public static final String COL_SEND_TIME = "send_time";
    public static final String COL_PROCESS_STATE = "process_state";
    public static final String COL_VIEW_STATE = "view_state";
    public static final String COL_TODO_TYPE = "todo_type";
    public static final String COL_RELATE_LINK = "relate_link";
    public static final String COL_ORIGIN_JSON = "origin_json";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 源app id
     */
    @TableField(value = "src_app")
    private String srcApp;
    /**
     * 源id，用于去重
     */
    @TableField(value = "src_id")
    private String srcId;
    /**
     * 待办标题
     */
    @TableField(value = "todo_title")
    private String todoTitle;
    /**
     * 待办内容
     */
    @TableField(value = "todo_content")
    private String todoContent;
    /**
     * 关联用户
     */
    @TableField(value = "target_user")
    private String targetUser;
    @TableField(value = "send_time")
    private LocalDateTime sendTime;
    /**
     * 处理状态，字典TODO_PROCESS_STATE
     */
    @TableField(value = "process_state")
    private Integer processState;
    /**
     * 0-未读，1-已读
     */
    @TableField(value = "view_state")
    private Integer viewState;
    /**
     * 待办类型，0-流程待办，1-任务待办
     */
    @TableField(value = "todo_type")
    private Integer todoType;
    /**
     * 关联链接
     */
    @TableField(value = "relate_link")
    private String relateLink;
    /**
     * 用于调试
     */
    @TableField(value = "origin_json")
    private String originJson;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}