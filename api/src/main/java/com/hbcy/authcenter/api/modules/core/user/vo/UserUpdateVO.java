package com.hbcy.authcenter.api.modules.core.user.vo;

import com.hbcy.authcenter.api.common.constants.G;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author 姚泰然
 * @date 2025-12-26 19:40
 */
@Data
public class UserUpdateVO {
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = G.PHONE_PATTERN, message = "手机号格式不正确")
    private String phone;
    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    @Length(min = 3, max = 50, message = "账号长度必须在3到50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "账号仅能包含数字、字母、下划线和中划线")
    private String account;
    /**
     * 姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Length(min = 2, max = 20, message = "真实姓名长度必须在2到20之间")
    private String realName;
    /**
     * 邮箱
     */
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;
    /**
     * 头像
     */
    private String avatar;
}
