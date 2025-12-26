package com.hbcy.authcenter.api.modules.core.user.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户自己重置密码
 *
 * @author 姚泰然
 * @date 2025-12-26 09:29
 */
@Data
public class UserResetPasswdVO {
    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPasswd;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String password;
}
