package com.hbcy.authcenter.api.modules.sys.dict.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 字典值更新vo
 * @author 姚泰然
 * @date 2026-01-14 17:24
 */
@Data
public class DictUpdateVO {
    /**
     * 字典编码
     */
    @NotBlank(message = "valueStr不能为空")
    @Length(max = 100, message = "valueStr长度不能超过100")
    private String valueStr;
    /**
     * 字典名称
     */
    @NotBlank(message = "valueCn不能为空")
    @Length(max = 100, message = "valueCn长度不能超过100")
    private String valueCn;
    /**
     * 禁用状态，0-正常，1-禁用
     */
    private int forbidden;
}
