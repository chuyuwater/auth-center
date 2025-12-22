package com.hbcy.authcenter.modules.sys.dict.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author 姚泰然
 * @date 2025-12-22 13:45
 */
@Data
public class CreateOrUpdateSysDictVO {
    @NotBlank(message = "featCode不能为空")
    @Length(max = 50, message = "featCode长度不能超过50")
    private String featCode;
    @NotBlank(message = "valueStr不能为空")
    @Length(max = 100, message = "valueStr长度不能超过100")
    private String valueStr;
    @NotBlank(message = "valueCn不能为空")
    @Length(max = 100, message = "valueCn长度不能超过100")
    private String valueCn;
    private String parentId = "";
    private Integer showOrder = 0;
}