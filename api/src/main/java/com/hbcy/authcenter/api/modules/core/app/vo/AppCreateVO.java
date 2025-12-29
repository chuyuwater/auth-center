package com.hbcy.authcenter.api.modules.core.app.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
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
    private boolean multiTenancy;
}
