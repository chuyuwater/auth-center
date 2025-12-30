package com.hbcy.authcenter.api.modules.core.org.vo;

import com.hbcy.authcenter.api.common.enums.OrgNodeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

/**
 * @author 姚泰然
 * @date 2025-12-25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OrgTreeCreateVO extends OrgTreeUpdateVO {
    /**
     * 父节点
     * 创建时，节点固定在同级末尾。
     */
    private String parentId = "";

    /**
     * 节点类型，组织或者部门
     */
    @Range(min = 0, max = 1, message = "节点类型只能为0-1")
    private Integer nodeType = OrgNodeTypeEnum.ORG.getValue();
}
