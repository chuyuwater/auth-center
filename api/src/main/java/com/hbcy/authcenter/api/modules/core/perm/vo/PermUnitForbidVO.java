package com.hbcy.authcenter.api.modules.core.perm.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 权限单元启用、禁用切换
 * @author 姚泰然
 * @date 2026-01-13 14:16
 */
@Data
public class PermUnitForbidVO {
    /**
     * 权限单元id
     */
    @NotBlank(message = "权限单元id不能为空")
    private String unitId;
    /**
     * 禁用状态
     */
    private Integer forbidden = 0;
}
