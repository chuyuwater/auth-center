package com.hbcy.authcenter.api.modules.core.org.vo;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2025-12-25
 */
@Data
public class OrgTreeQueryVO {
    /**
     * 父节点ID，若为空则查询整个树
     */
    private String parentId;

    /**
     * 节点类型，0-组织，1-部门
     */
    private Integer nodeType;

    /**
     * 节点类别，0-项目部，1-公司，2-子公司，3-分公司
     */
    private Integer nodeCategory;

    /**
     * 关键字
     */
    private String keyword;

}
