package com.hbcy.authcenter.api.modules.sys.dict.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author 姚泰然
 * @date 2025-12-22 13:45
 */
@Data
public class DictUpsertVO {
    /**
     * 字典key
     */
    @NotBlank(message = "featCode不能为空")
    @Length(max = 50, message = "featCode长度不能超过50")
    private String featCode;
    /**
     * 字典value
     */
    @NotBlank(message = "valueStr不能为空")
    @Length(max = 100, message = "valueStr长度不能超过100")
    private String valueStr;
    /**
     * 中文注释
     */
    @NotBlank(message = "valueCn不能为空")
    @Length(max = 100, message = "valueCn长度不能超过100")
    private String valueCn;
    /**
     * 父级节点，为空表示根节点
     */
    private String parentId = "";
    /**
     * 显示顺序
     */
    private Integer showOrder = 0;
}