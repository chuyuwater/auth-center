package com.hbcy.authcenter.api.modules.core.tenant.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * @author 姚泰然
 * @date 2025-12-23 14:35
 */
@Data
public class TenantForbiddenVO {
    /**
     * 租户id
     */
    @NotBlank(message = "租户id不能为空")
    private String tenantId;
    /**
     * 是否禁用，0-正常，1-禁用
     */
    @Range(min = 0, max = 1, message = "禁用状态只能为0或1")
    private int forbidden;
}
