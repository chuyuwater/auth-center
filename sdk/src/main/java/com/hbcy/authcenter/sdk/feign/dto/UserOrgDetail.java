package com.hbcy.authcenter.sdk.feign.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-03-18 12:25
 */
@Data
public class UserOrgDetail {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 组织id
     */
    private String orgId;
    /**
     * 实际挂载的组织或部门id
     */
    private String nodeId;
    /**
     * 组织类型，0-组织，1-部门
     */
    private Integer nodeType;
    /**
     * 组织名称
     */
    private String orgName;
    /**
     * 全路径（id）
     */
    private String idPath;
    /**
     * 组织是否被禁用
     */
    private Integer orgForbidden;
    /**
     * 全路径（名字，逆序）
     */
    private String namePath;
    /**
     * 组织类别，字典：ORG_CATEGORY
     */
    private Integer nodeCategory;
    /**
     * 组织类别名称
     */
    private String nodeCategoryName;
    /**
     * 是否主职组织
     */
    private Integer mainJob;
}
