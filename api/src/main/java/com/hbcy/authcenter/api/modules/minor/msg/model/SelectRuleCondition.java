package com.hbcy.authcenter.api.modules.minor.msg.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 选人规则条件
 */
@Data
@NoArgsConstructor
@TableName(value = "select_rule_condition")
public class SelectRuleCondition {
    public static final String COL_ID = "id";
    public static final String COL_RULE_ID = "rule_id";
    public static final String COL_COND_TYPE = "cond_type";
    public static final String COL_COND_OP = "cond_op";
    public static final String COL_COND_VALUES = "cond_values";
    public static final String COL_SHOW_ORDER = "show_order";
    public static final String COL_CREATE_TIME = "create_time";

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @TableField(value = "rule_id")
    private String ruleId;
    @TableField(value = "cond_type")
    private String condType;
    @TableField(value = "cond_op")
    private String condOp;
    @TableField(value = "cond_values")
    private String condValues;
    @TableField(value = "show_order")
    private Integer showOrder;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
