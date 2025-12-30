package com.hbcy.authcenter.api.modules.core.auth.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author 姚泰然
 * @date 2025-12-30
 */
@Data
public class LoginVO {
    /**
     * 账号
     */
    private String account;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    /**
     * 验证码ID
     */
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;

    /**
     * 用户输入的验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    /**
     * 当有多个租户时可选
     */
    private String tenantId;
}
