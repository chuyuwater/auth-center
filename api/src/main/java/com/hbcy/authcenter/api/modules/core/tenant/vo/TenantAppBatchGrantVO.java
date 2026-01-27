package com.hbcy.authcenter.api.modules.core.tenant.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

/**
 * 批量应用授权，默认授权应用的所有功能
 * @author 姚泰然
 * @date 2026-01-27 17:08
 */
@Data
public class TenantAppBatchGrantVO {
    /**
     * 租户ID
     */
    @NotBlank(message = "租户ID不能为空")
    private String tenantId;
    /**
     * 应用ID列表
     */
    @NotEmpty(message = "应用ID列表不能为空")
    private Set<String> appIds;
}
