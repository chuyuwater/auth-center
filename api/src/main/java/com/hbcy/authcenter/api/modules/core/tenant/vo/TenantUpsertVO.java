package com.hbcy.authcenter.api.modules.core.tenant.vo;

import com.hbcy.authcenter.api.common.constants.G;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author 姚泰然
 * @date 2025-12-23 14:28
 */
@Data
public class TenantUpsertVO {
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

    /**
     * 联系人
     */
    @Length(max = 20, message = "联系人长度不能超过20")
    @NotBlank(message = "联系人不能为空")
    private String contactUser;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = G.PHONE_PATTERN, message = "手机号格式不正确")
    private String contactPhone;
}
