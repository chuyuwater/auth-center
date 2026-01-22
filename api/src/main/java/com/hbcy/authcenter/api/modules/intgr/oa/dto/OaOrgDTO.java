package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import lombok.Data;

/**
 * Oa返回的组织数据
 * @author 姚泰然
 * @date 2026-01-21 10:52
 */
@Data
public class OaOrgDTO {
    /**
     * 节点id
     */
    private String id;
    /**
     * 是否还有子级
     */
    private Boolean isParent;
    /**
     * 是否虚拟节点,0-否，1-是
     */
    private String isVirtual;
    /**
     * 节点名称
     */
    private String name;
    /**
     * 父级id
     */
    private String pid;
    /**
     * 子公司id
     */
    private String psubcompanyid;
    /**
     * 节点完整路径
     */
    private String title;
    /**
     * 节点类型
     */
    private String type;
    /****************************************下面是注入的字段**************************************/
    /**
     * 我方relate_id
     */
    private String myRelateId;
    /**
     * 是否我方新建
     */
    private boolean myNew;
    /**
     * 我方id
     */
    private String myId;
    /**
     * 我方父级id
     */
    private String myParentId;
    /**
     * 显示顺序
     */
    private Integer myOrder;
    /**
     * 我方id_path
     */
    private String myIdPath;
}
