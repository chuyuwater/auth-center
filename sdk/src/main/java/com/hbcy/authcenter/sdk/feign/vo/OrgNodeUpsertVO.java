package com.hbcy.authcenter.sdk.feign.vo;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * 创建组织或部门
 * @author 姚泰然
 * @date 2026-03-26 18:14
 */
@Data
public class OrgNodeUpsertVO {
    /**
     * 节点类型，0-组织，1-部门
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private Integer nodeType = 0;
    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 简称
     */
    private String shortName;

    /**
     * 说明
     */
    private String memo = "业务系统自动创建";
    /**
     * 父节点
     * 一般是当前公司，不同环境id不一样
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private String parentId = "";

    /**
     * 组织类别
     * 0-项目部
     * 1-集团
     * 2-公司
     * 3-子公司
     * 4-分公司
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private Integer nodeCategory = 0;

    /**
     * 存在形式，0-实体，1-虚拟
     */
    @Range(min = 0, max = 1, message = "存在类型只能为0-1")
    @JsonSetter(nulls = Nulls.SKIP)
    private Integer existType = 0;
}
