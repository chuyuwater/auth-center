package com.hbcy.authcenter.sdk.feign.dto;

import lombok.Data;

/**
 * 组织节点数据
 * @author 姚泰然
 * @date 2026-01-23 14:29
 */
@Data
public class OrgNodeDTO {
    public static final int TYPE_ORG = 0;
    public static final int TYPE_DEP = 1;
    public static final int CATEGORY_PROJECT = 0;
    /**
     * 组织/部门id
     * 组织id内有"-ORG-"，部门id内有"-DEP-"
     */
    private String id;
    /**
     * 节点名
     */
    private String nodeName;
    /**
     * 简称
     */
    private String shortName;
    /**
     * 说明
     */
    private String memo;
    /**
     * 节点类型，0-组织，1-部门
     */
    private Integer nodeType;
    /**
     * 存在形式：0-实体，1-虚拟
     */
    private Integer existType;
    /**
     * 类别，组织：0-项目部，其他：字典ORG_CATEGORY
     */
    private Integer nodeCategory;
    /**
     * 父节点id
     */
    private String parentId;
    /**
     * 节点路径
     */
    private String idPath;
    /**
     * 租户id
     */
    private String tenantId;
    /**
     * 关联id
     * 如果是项目部，关联的是项目管理系统中项目id
     */
    private String relateId;
}
