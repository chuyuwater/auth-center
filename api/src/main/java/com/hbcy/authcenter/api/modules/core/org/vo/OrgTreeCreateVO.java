package com.hbcy.authcenter.api.modules.core.org.vo;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.hbcy.authcenter.api.common.enums.OrgNodeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

/**
 * 组织节点创建VO
 *
 * @author 姚泰然
 * @date 2025-12-25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OrgTreeCreateVO extends OrgTreeUpdateVO {
    /**
     * 节点类型，0-组织，1-部门
     */
    @JsonSetter(nulls = Nulls.SKIP)
    @Range(min = 0, max = 1, message = "节点类型只能为0-1")
    private Integer nodeType = OrgNodeTypeEnum.ORG.getValue();
}
