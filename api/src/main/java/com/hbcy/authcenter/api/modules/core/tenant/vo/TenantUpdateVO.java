package com.hbcy.authcenter.api.modules.core.tenant.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 租户更新VO
 *
 * @author 姚泰然
 * @date 2025-12-28 18:57
 */
@Data
public class TenantUpdateVO {
    /**
     * 租户名称
     */
    @Length(max = 20, message = "租户名称长度必须在1到20之间")
    @NotBlank(message = "租户名称不能为空")
    private String nameCn;

    @Length(max = 12, message = "租户简称长度不能超过12")
    @NotBlank(message = "租户简称不能为空")
    private String shortName;

    /**
     * logo的url
     */
    @Length(max = 255, message = "logo的url长度不能超过255")
    private String logo;

    /**
     * 备注
     */
    @Length(max = 200, message = "备注长度不能超过200")
    private String memo;
}
