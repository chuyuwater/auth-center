package com.hbcy.authcenter.api.modules.sys.dict.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

/**
 * 字典更新/创建VO
 *
 * @author 姚泰然
 * @date 2025-12-22 13:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DictCreateVO extends DictUpdateVO {
    /**
     * 对于分组下的一级节点，parentId即分组的id，否则为父节点id
     */
    @NotBlank(message = "parentId不能为空")
    private String parentId;


    /**
     * 字典类型，0-列表，1-树状
     */
    @Range(min = 0, max = 1, message = "dictType范围0-1")
    private int dictType;
}