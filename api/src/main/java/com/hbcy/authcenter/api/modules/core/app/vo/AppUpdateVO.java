package com.hbcy.authcenter.api.modules.core.app.vo;

import jakarta.validation.constraints.Min;
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
    @Length(max = 200, message = "应用简介长度不能超过200个字符")
    private String memo;
    /**
     * 显示顺序，越小越靠前
     */
    @Min(value = 0, message = "显示顺序不能小于0")
    private int showOrder;
    /**
     * 应用图标
     */
    private String icon = "";
}
