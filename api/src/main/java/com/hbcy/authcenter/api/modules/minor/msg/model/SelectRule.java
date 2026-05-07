package com.hbcy.authcenter.api.modules.minor.msg.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 选人规则
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@TableName(value = "select_rule")
@Accessors(chain = true)
public class SelectRule extends BaseEntity {
    public static final String COL_ID = "id";
    public static final String COL_RULE_NO = "rule_no";
    public static final String COL_RULE_NAME = "rule_name";
    public static final String COL_BIZ_ENTITY = "biz_entity";
    public static final String COL_GROUP_ID = "group_id";
    public static final String COL_REF_COUNT = "ref_count";
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
    @TableField(value = "rule_no")
    private String ruleNo;
    @TableField(value = "rule_name")
    private String ruleName;
    @TableField(value = "biz_entity")
    private String bizEntity;
    @TableField(value = "group_id")
    private String groupId;
    @TableField(value = "ref_count")
    private Integer refCount;
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
