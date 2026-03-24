package com.hbcy.authcenter.sdk.feign.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 组织节点查询VO
 *
 * @author 姚泰然
 * @date 2025-12-25
 */
@Data
@Accessors(chain = true)
public class OrgNodeQueryVO {
    /**
     * 父节点ID，若为空则查询整个树
     */
    private String parentId;

    /**
     * 节点类型，0-组织，1-部门
     */
    private Integer nodeType;

    /**
     * 节点类别，组织：0-项目部，其他：字典ORG_CATEGORY
     */
    private Integer nodeCategory;

    /**
     * 禁用状态，0-启用，1-禁用
     */
    private Integer forbidden;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 是否返回parentId本级
     */
    private boolean returnParent;
}
