package com.hbcy.authcenter.api.modules.core.perm.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-27 20:34
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@TableName(value = "perm_unit")
public class PermUnit extends BaseEntity {
    /**
     * 角色ID模板
     */
    public static final String ROLE_ID_TEMPLATE = "%s-ROLE-%06d";
    public static final int RBAC = 0;
    public static final int ABAC = 1;
    /**
     * 策略ID模板
     */
    public static final String POLICY_ID_TEMPLATE = "%s-POLICY-%06d";
    public static final String COL_ID = "id";
    public static final String COL_NAME_CN = "name_cn";
    public static final String COL_POLICY_MODEL = "policy_model";
    public static final String COL_BELONG_TO = "belong_to";
    public static final String COL_MEMO = "memo";
    public static final String COL_FORBIDDEN = "forbidden";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_DELETE_TIME = "delete_time";
    /**
     * 规则生成名称
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    /**
     * 名称
     */
    @TableField(value = "name_cn")
    private String nameCn;
    /**
     * 授权模型，0-RBAC，1-ABAC
     */
    @TableField(value = "policy_model")
    private Integer policyModel;
    /**
     * 归属分组
     */
    @TableField(value = "belong_to")
    private String belongTo;
    /**
     * 说明
     */
    @TableField(value = "memo")
    private String memo;
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
    @TableField(value = "delete_time", fill = FieldFill.UPDATE)
    @TableLogic(value = "0", delval = "-1")
    private Long deleteTime;
}