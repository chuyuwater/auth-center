package com.hbcy.authcenter.api.modules.core.org.vo;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.hbcy.authcenter.api.common.enums.OrgNodeCategoryEnum;
import com.hbcy.common.db.dictvalue.DictValid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * 组织节点更新VO
 *
 * @author 姚泰然
 * @date 2025-12-25
 */
@Data
public class OrgTreeUpdateVO {
    /**
     * 节点名称
     */
    @NotBlank(message = "名称不能为空")
    @Length(max = 50, message = "名称长度不能超过50")
    private String nodeName;

    @NotBlank(message = "简称不能为空")
    @Length(max = 12, message = "简称长度不能超过12")
    private String shortName;

    @Length(max = 200, message = "说明长度不能超过200")
    private String memo;
    /**
     * 父节点
     * 创建或更新父节点时，节点固定在同级末尾。
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private String parentId = "";

    /**
     * 组织类别，字典ORG_CATEGORY
     */
    @JsonSetter(nulls = Nulls.SKIP)
    @DictValid(dictKey = "ORG_CATEGORY")
    private Integer nodeCategory = OrgNodeCategoryEnum.COMPANY.getValue();

    /**
     * 存在形式，0-实体，1-虚拟
     */
    @Range(min = 0, max = 1, message = "存在类型只能为0-1")
    private Integer existType;
}
