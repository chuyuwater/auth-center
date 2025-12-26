package com.hbcy.authcenter.api.modules.core.user.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户自己重置密码
 *
 * @author 姚泰然
 * @date 2025-12-26 09:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserResetPasswdVO extends AdminResetPasswdVO {
    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPasswd;
}
