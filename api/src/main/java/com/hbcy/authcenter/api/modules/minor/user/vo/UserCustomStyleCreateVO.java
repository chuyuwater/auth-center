package com.hbcy.authcenter.api.modules.minor.user.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author 姚泰然
 * @date 2026-04-03 15:22
 */
@Data
public class UserCustomStyleCreateVO {
    /**
     * 自定义项，字典USER_CUSTOM_STYLE
     */
    @NotBlank(message = "item不能为空")
    @Length(max = 100, message = "item长度不能超过100个字符")
    private String item;
    /**
     * 用户设置
     */
    @NotNull(message = "setting不能为空")
    @Length(max = 255, message = "setting长度不能超过255个字符")
    private String setting;
}
