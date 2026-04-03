package com.hbcy.authcenter.api.modules.minor.user.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.common.db.dictvalue.DictInject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户自定义页面风格
 * @author 姚泰然
 * @date 2026-04-03 14:40
 */
@Data
@NoArgsConstructor
@TableName(value = "user_custom_style")
public class UserCustomStyle {
    public static final String COL_ID = "id";
    public static final String COL_ITEM = "item";
    public static final String COL_SETTING = "setting";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPTIME_TIME = "update_time";
    /**
     * ulid
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 自定义项，字典USER_CUSTOM_STYLE
     */
    @TableField(value = "item")
    @DictInject(value = "USER_CUSTOM_STYLE")
    private String item;
    /**
     * 用户设置，一般也是字典值
     */
    @TableField(value = "setting")
    private String setting;
    /**
     * 用户
     */
    @TableField(value = "user_id")
    private String userId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}