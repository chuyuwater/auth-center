package com.hbcy.authcenter.api.modules.core.tenant.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 姚泰然
 * @date 2025-12-23 17:33
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TenantAppGrantVO extends TenantAppGrantUpdateVO {
    /**
     * 应用id
     */
    @NotBlank(message = "应用ID不能为空")
    private String appId;
    /**
     * 租户id
     */
    @NotBlank(message = "租户ID不能为空")
    private String tenantId;
}
