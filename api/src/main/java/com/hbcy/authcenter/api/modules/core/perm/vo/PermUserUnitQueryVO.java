package com.hbcy.authcenter.api.modules.core.perm.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-02-24 10:29
 */
@Data
public class PermUserUnitQueryVO {
    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    /**
     * 组织ID
     */
    private String orgId;
    /**
     * 权限单元ID
     */
    private String unitId;
}
