package com.hbcy.authcenter.api.modules.core.app.vo;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 应用更新VO
 *
 * @author 姚泰然
 * @date 2025-12-23 16:25
 */
@Data
public class AppUpdateVO {
    /**
     * 中文名，也不能重复
     */
    @NotBlank(message = "应用中文名不能为空")
    @Length(max = 20, message = "应用中文名长度不能超过20个字符")
    private String nameCn;
    /**
     * 简介
     */
    @JsonSetter(nulls = Nulls.SKIP)
    @Length(max = 200, message = "应用简介长度不能超过200个字符")
    private String memo = "";
    /**
     * 应用图标
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private String icon = "";
    /**
     * 应用URL，仅当应用类型为外部应用时使用，用于三方认证；可修改
     */
    @JsonSetter(nulls = Nulls.SKIP)
    @Length(max = 512, message = "应用URL长度不能超过512个字符")
    private String appUrl = "";
}
