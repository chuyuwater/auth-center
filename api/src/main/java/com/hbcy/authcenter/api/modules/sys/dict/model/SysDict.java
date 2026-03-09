package com.hbcy.authcenter.api.modules.sys.dict.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2026-01-15 12:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@TableName(value = "sys_dict")
public class SysDict extends BaseEntity {
    public static final int DICT_TYPE_LIST = 0;
    public static final int DICT_TYPE_TREE = 1;

    public static final String COL_ID = "id";
    public static final String COL_APP_ID = "app_id";
    public static final String COL_FEAT_CODE = "feat_code";
    public static final String COL_VALUE_STR = "value_str";
    public static final String COL_VALUE_CN = "value_cn";
    public static final String COL_DICT_TYPE = "dict_type";
    public static final String COL_PARENT_ID = "parent_id";
    public static final String COL_ID_PATH = "id_path";
    public static final String COL_SHOW_ORDER = "show_order";
    public static final String COL_FORBIDDEN = "forbidden";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_DELETE_TIME = "delete_time";
    /**
     * ulid
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 应用id，为空标识通用
     */
    @TableField(value = "app_id")
    private String appId;
    /**
     * 字典键，为空表示分组
     */
    @TableField(value = "feat_code")
    private String featCode;
    /**
     * 字典值
     */
    @TableField(value = "value_str")
    private String valueStr;
    /**
     * 中文值
     */
    @TableField(value = "value_cn")
    private String valueCn;
    /**
     * 0-列表，1-树状
     */
    @TableField(value = "dict_type")
    private Integer dictType;
    /**
     * 父节点
     */
    @TableField(value = "parent_id")
    private String parentId;
    @TableField(value = "id_path")
    private String idPath;
    /**
     * 显示顺序
     */
    @TableField(value = "show_order")
    private Integer showOrder;
    @TableField(value = "forbidden")
    private Integer forbidden;
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