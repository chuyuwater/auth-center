package com.hbcy.authcenter.api.modules.core.inner.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-01-07 16:28
 */
@Data
public class RefreshUserPermVO {
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    @NotBlank(message = "组织ID不能为空")
    private String orgId;
    @NotBlank(message = "租户ID不能为空")
    private String tenantId;
}
