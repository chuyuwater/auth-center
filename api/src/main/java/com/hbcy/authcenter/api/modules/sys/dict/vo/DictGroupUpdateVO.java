package com.hbcy.authcenter.api.modules.sys.dict.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典更新
 * @author 姚泰然
 * @date 2026-01-14 17:14
 */
@Data
public class DictGroupUpdateVO {
    /**
     * 字典类型
     */
    @NotBlank(message = "字典类型不能为空")
    private String valueCn;
    /**
     * 类型编码，唯一
     */
    @NotBlank(message = "类型编码不能为空")
    private String valueStr;
    /**
     * 说明
     */
    private String memo;
}
