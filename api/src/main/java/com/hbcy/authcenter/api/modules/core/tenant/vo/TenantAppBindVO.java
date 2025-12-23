package com.hbcy.authcenter.api.modules.core.tenant.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author 姚泰然
 * @date 2025-12-23 17:33
 */
@Data
public class TenantAppBindVO {
    @NotBlank(message = "应用ID不能为空")
    private String appId;
    @NotBlank(message = "租户ID不能为空")
    private String tenantId;
}
