package com.hbcy.authcenter.api.modules.core.org.vo;

import com.hbcy.authcenter.api.common.enums.OrgNodeCategoryEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
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

    /**
     * 组织类型，字典维护，默认公司
     */
    @Range(min = 0, max = 3, message = "组织类型只能为0-3")
    private Integer nodeCategory = OrgNodeCategoryEnum.COMPANY.getValue();

    /**
     * 存在形式，0-实体，1-虚拟
     */
    @Range(min = 0, max = 1, message = "存在类型只能为0-1")
    private Integer existType;
}
