package com.hbcy.authcenter.api.modules.core.user.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 禁用/启用用户
 *
 * @author 姚泰然
 * @date 2025-12-26 09:30
 */
@Data
public class ForbidUserVO {
    /**
     * 用户id
     */
    @NotBlank(message = "用户id不能为空")
    private String userId;
    /**
     * 封禁或者解封
     */
    private boolean forbid = false;
}
