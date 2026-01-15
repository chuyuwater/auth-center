package com.hbcy.authcenter.api.modules.sys.dict.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 字典类型更新/创建VO
 * 字典类型的featCode固定为空字符串
 * @author 姚泰然
 * @date 2026-01-14 17:06
 */
@Data
public class DictGroupUpsertVO {
    /**
     * 应用ID
     */
    @NotNull(message = "必须指定应用id")
    private String appId;
    /**
     * 字典类型
     */
    private String valueCn;
    /**
     * 类型编码，唯一
     */
    private String valueStr;

}
