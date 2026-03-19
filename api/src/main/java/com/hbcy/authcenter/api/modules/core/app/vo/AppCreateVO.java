package com.hbcy.authcenter.api.modules.core.app.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * 应用创建VO
 *
 * @author 姚泰然
 * @date 2025-12-23 16:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppCreateVO extends AppUpdateVO {
    /**
     * 用户指定唯一英文标识，不能重复
     */
    @Length(max = 50, message = "应用id长度不能超过50个字符")
    @NotBlank(message = "应用id不能为空")
    private String id;

    /**
     * 是否多租户
     */
    private Boolean multiTenancy = false;

    /**
     * 应用类型：null/0-未选，1-平台应用，2-外部应用；新增后不可修改
     */
    private Integer appType;

    /**
     * 应用URL，仅当应用类型为外部应用时必填，用于三方认证
     */
    @Length(max = 512, message = "应用URL长度不能超过512个字符")
    private String appUrl;
}
