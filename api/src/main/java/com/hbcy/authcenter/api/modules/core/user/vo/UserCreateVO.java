package com.hbcy.authcenter.api.modules.core.user.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * 创建用户需要的数据
 *
 * @author 姚泰然
 * @date 2025-12-26 08:55
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserCreateVO extends UserUpdateVO {
    /**
     * 创建时归属组织，即为其默认主职组织
     */
    @NotBlank(message = "归属组织不能为空")
    private String nodeId;
    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    @Length(min = 3, max = 50, message = "账号长度必须在3到50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "账号仅能包含数字、字母、下划线")
    private String account;
}
