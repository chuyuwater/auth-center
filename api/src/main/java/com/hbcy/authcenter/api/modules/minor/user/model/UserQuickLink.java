package com.hbcy.authcenter.api.modules.minor.user.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 快捷入口
 * @author 姚泰然
 * @date 2026-02-25 16:09
 */

@Data
@NoArgsConstructor
@TableName(value = "user_quick_link")
public class UserQuickLink {
    public static final String COL_ID = "id";
    public static final String COL_RES_ID = "res_id";
    public static final String COL_SHOW_ORDER = "show_order";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_ORG_ID = "org_id";
    public static final String COL_CLIENT_TYPE = "client_type";
    public static final String COL_CREATE_TIME = "create_time";
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 菜单id
     */
    @TableField(value = "res_id")
    private String resId;
    /**
     * 显示顺序
     */
    @TableField(value = "show_order")
    private Integer showOrder;
    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private String userId;
    /**
     * 组织id
     */
    @TableField(value = "org_id")
    private String orgId;
    /**
     * 1-PC端，2-移动端
     */
    @TableField(value = "client_type")
    private Integer clientType;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}