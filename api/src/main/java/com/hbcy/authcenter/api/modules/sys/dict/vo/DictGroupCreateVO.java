package com.hbcy.authcenter.api.modules.sys.dict.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

/**
 * 字典类型创建VO
 * 字典类型的featCode固定为空字符串
 * @author 姚泰然
 * @date 2026-01-14 17:06
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DictGroupCreateVO extends DictGroupUpdateVO {
    /**
     * 应用ID
     */
    @NotNull(message = "必须指定应用id")
    private String appId;
    /**
     * 字典类型, 0-列表，1-树状
     */
    @Range(min = 0, max = 1, message = "字典类型只能为0或1")
    private int dictType;
}
