package com.hbcy.authcenter.api.modules.core.user.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author 姚泰然
 * @date 2025-12-26 09:26
 */
@Data
public class AdminResetPasswdVO {
    /**
     * 用户id
     */
    @NotBlank(message = "用户id不能为空")
    private String userId;
    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String password;
}
