package com.hbcy.authcenter.api.modules.core.org.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 姚泰然
 * @date 2025-12-25 10:59
 */
@Data
@NoArgsConstructor
@TableName(value = "org_tree")
public class OrgTree {
    /**
     * 组织/部门id，算法生成
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 节点名
     */
    @TableField(value = "node_name")
    private String nodeName;

    /**
     * 节点类型，0-虚拟节点，1-组织，2-部门
     */
    @TableField(value = "node_type")
    private Integer nodeType;

    /**
     * 类别，组织：0-公司，1-分公司，2-子公司，3-项目部
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

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "create_user")
    private String createUser;

    @TableField(value = "update_user")
    private String updateUser;

    public static final String COL_ID = "id";

    public static final String COL_NODE_NAME = "node_name";

    public static final String COL_NODE_TYPE = "node_type";

    public static final String COL_NODE_CATEGORY = "node_category";

    public static final String COL_PARENT_ID = "parent_id";

    public static final String COL_ID_PATH = "id_path";

    public static final String COL_SHOW_ORDER = "show_order";

    public static final String COL_TENANT_ID = "tenant_id";

    public static final String COL_CREATE_TIME = "create_time";

    public static final String COL_UPDATE_TIME = "update_time";

    public static final String COL_CREATE_USER = "create_user";

    public static final String COL_UPDATE_USER = "update_user";
}