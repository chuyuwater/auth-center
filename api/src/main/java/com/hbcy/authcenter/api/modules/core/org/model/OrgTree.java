package com.hbcy.authcenter.api.modules.core.org.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-25 17:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@TableName(value = "org_tree")
public class OrgTree extends BaseEntity {
    public static final String ORG_ID_TEMPLATE = "%s-ORG-%06d";
    public static final String DEPT_ID_TEMPLATE = "%s-DEP-%06d";
    public static final int EXIST_TYPE_ENTITY = 0;
    public static final int EXIST_TYPE_VIRTUAL = 1;
    public static final String COL_ID = "id";
    public static final String COL_NODE_NAME = "node_name";
    public static final String COL_SHORT_NAME = "short_name";
    public static final String COL_MEMO = "memo";
    public static final String COL_NODE_TYPE = "node_type";
    public static final String COL_EXIST_TYPE = "exist_type";
    public static final String COL_NODE_CATEGORY = "node_category";
    public static final String COL_PARENT_ID = "parent_id";
    public static final String COL_ID_PATH = "id_path";
    public static final String COL_SHOW_ORDER = "show_order";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_DELETE_TIME = "delete_time";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    /**
     * 组织/部门id，算法生成
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    /**
     * 节点名
     */
    @TableField(value = "node_name")
    private String nodeName;
    /**
     * 简称
     */
    @TableField(value = "short_name")
    private String shortName;
    /**
     * 说明
     */
    @TableField(value = "memo")
    private String memo;
    /**
     * 节点类型，0-组织，1-部门
     */
    @TableField(value = "node_type")
    private Integer nodeType;
    /**
     * 存在形式：0-实体，1-虚拟
     */
    @TableField(value = "exist_type")
    private Integer existType;
    /**
     * 类别，组织：0-项目部，1-公司，2-分公司，3-子公司
     */
    @TableField(value = "node_category")
    private Integer nodeCategory;
    /**
     * 父节点id
     */
    @TableField(value = "parent_id")
    private String parentId;
    /**
     * 全路径，方便查询
     */
    @TableField(value = "id_path")
    private String idPath;
    @TableField(value = "show_order")
    private Integer showOrder;
    /**
     * 租户id
     */
    @TableField(value = "tenant_id")
    private String tenantId;
    /**
     * 逻辑删除
     */
    @TableField(value = "delete_time")
    private @TableLogic(value = "0", delval = "-1") Long deleteTime;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(value = "create_user")
    private String createUser;
    @TableField(value = "update_user")
    private String updateUser;
}