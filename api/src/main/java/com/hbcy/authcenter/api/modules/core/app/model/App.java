package com.hbcy.authcenter.api.modules.core.app.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-23 17:10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@TableName(value = "app")
public class App extends BaseEntity {
    public static final String COL_ID = "id";
    public static final String COL_NAME_CN = "name_cn";
    public static final String COL_MEMO = "memo";
    public static final String COL_SHOW_ORDER = "show_order";
    public static final String COL_ICON = "icon";
    public static final String COL_FORBIDDEN = "forbidden";
    public static final String COL_SUB_DOM = "sub_dom";
    public static final String COL_OBJ_DOM = "obj_dom";
    public static final String COL_BINDING_TENANT = "binding_tenant";
    public static final String COL_DELETE_TIME = "delete_time";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    public static final String BINDING_PLACEHOLDER = "-";
    /**
     * 应用英文标识
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    /**
     * 中文名
     */
    @TableField(value = "name_cn")
    private String nameCn;
    /**
     * 简介
     */
    @TableField(value = "memo")
    private String memo;
    /**
     * 显示顺序
     */
    @TableField(value = "show_order")
    private Integer showOrder;
    /**
     * 图标路径
     */
    @TableField(value = "icon")
    private String icon;
    /**
     * 是否禁用
     */
    @TableField(value = "forbidden")
    private Integer forbidden;
    /**
     * 主体域：0-全局，1-组织，2-岗位
     */
    @TableField(value = "sub_dom")
    private Integer subDom;
    /**
     * 客体域
     */
    @TableField(value = "obj_dom")
    private String objDom;
    /**
     * 空：多租户应用，"-": 单租户应用尚未绑定租户，其他：单租户应用绑定的租户id
     */
    @TableField(value = "binding_tenant")
    private String bindingTenant;
    @TableField(value = "delete_time")
    private Long deleteTime;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(value = "create_user")
    private String createUser;
    @TableField(value = "update_user")
    private String updateUser;
}