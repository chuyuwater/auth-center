package com.hbcy.authcenter.api.modules.core.perm.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-28 10:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@TableName(value = "perm_tree")
public class PermTree extends BaseEntity {
    public static final String RBAC_ID_TEMPLATE = "%s-ROLEGRP-%06d";
    public static final String ABAC_ID_TEMPLATE = "%s-POLICYGRP-%06d";
    public static final String COL_ID = "id";
    public static final String COL_NODE_NAME = "node_name";
    public static final String COL_MEMO = "memo";
    public static final String COL_PARENT_ID = "parent_id";
    public static final String COL_ID_PATH = "id_path";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_SHOW_ORDER = "show_order";
    public static final String COL_DELETE_TIME = "delete_time";
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    /**
     * 分组（节点）名称
     */
    @TableField(value = "node_name")
    private String nodeName;
    /**
     * 说明
     */
    @TableField(value = "memo")
    private String memo;
    /**
     * 父节点id
     */
    @TableField(value = "parent_id")
    private String parentId;
    @TableField(value = "id_path")
    private String idPath;
    @TableField(value = "show_order")
    private Integer showOrder;
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
    /**
     * 删除时间
     */
    @TableField(value = "delete_time")
    private Long deleteTime;
}