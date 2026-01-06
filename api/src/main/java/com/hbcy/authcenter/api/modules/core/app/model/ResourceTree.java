package com.hbcy.authcenter.api.modules.core.app.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-24 15:19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@TableName(value = "resource_tree")
public class ResourceTree extends BaseEntity {
    public static final String COL_ID = "id";
    public static final String COL_NAME_CN = "name_cn";
    public static final String COL_APP_ID = "app_id";
    public static final String COL_CUSTOM_ID = "custom_id";
    public static final String COL_PARENT_ID = "parent_id";
    public static final String COL_ID_PATH = "id_path";
    public static final String COL_CLIENT_TYPE = "client_type";
    public static final String COL_ICON = "icon";
    public static final String COL_ROUTE_LINK = "route_link";
    public static final String COL_SHOW_ORDER = "show_order";
    public static final String COL_HIDDEN = "hidden";
    public static final String COL_SHOW_LEVEL = "show_level";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 中文名
     */
    @TableField(value = "name_cn")
    private String nameCn;
    /**
     * 应用id
     */
    @TableField(value = "app_id")
    private String appId;
    /**
     * 应用开发者自定义菜单id
     */
    @TableField(value = "custom_id")
    private String customId;
    /**
     * 父节点
     */
    @TableField(value = "parent_id")
    private String parentId;
    /**
     * 节点全路径
     */
    @TableField(value = "id_path")
    private String idPath;
    /**
     * 支持的客户端类型，0-全端，1-PC端，2-移动端
     */
    @TableField(value = "client_type")
    private Integer clientType;
    /**
     * 图标地址
     */
    @TableField(value = "icon")
    private String icon;
    /**
     * 路由地址
     */
    @TableField(value = "route_link")
    private String routeLink;
    /**
     * 同级显示顺序
     */
    @TableField(value = "show_order")
    private Integer showOrder;
    /**
     * 是否隐藏，0-显示，1-隐藏
     */
    @TableField(value = "hidden")
    private Integer hidden;
    /**
     * 显示级别，0-全局，1-组织级，2-项目级
     */
    @TableField(value = "show_level")
    private Integer showLevel;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(value = "create_user")
    private String createUser;
    @TableField(value = "update_user")
    private String updateUser;
}