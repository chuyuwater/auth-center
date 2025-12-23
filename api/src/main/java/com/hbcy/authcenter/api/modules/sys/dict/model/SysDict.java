package com.hbcy.authcenter.api.modules.sys.dict.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-22 13:45
 */
@Data
@NoArgsConstructor
@TableName(value = "sys_dict")
public class SysDict {
    public static final String COL_DISP_ORDER = "disp_order";
    public static final String COL_ID = "id";
    public static final String COL_FEAT_CODE = "feat_code";
    public static final String COL_VALUE_STR = "value_str";
    public static final String COL_VALUE_CN = "value_cn";
    public static final String COL_PARENT_ID = "parent_id";
    public static final String COL_SHOW_ORDER = "show_order";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    /**
     * ulid
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 字典键
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
     * 父节点
     */
    @TableField(value = "parent_id")
    private String parentId;
    /**
     * 显示顺序
     */
    @TableField(value = "show_order")
    private Integer showOrder;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}