package com.hbcy.authcenter.gateway.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 姚泰然
 * @date 2026-01-07 16:28
 */
@Data
@Accessors(chain = true)
public class RefreshUserPermVO {
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    @NotBlank(message = "组织ID不能为空")
    private String orgId;
    @NotBlank(message = "租户ID不能为空")
    private String tenantId;
}
