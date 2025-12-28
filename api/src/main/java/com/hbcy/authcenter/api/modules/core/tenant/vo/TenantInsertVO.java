package com.hbcy.authcenter.api.modules.core.tenant.vo;

import com.hbcy.authcenter.api.common.constants.G;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * 管理员相关信息后续由租户自行维护
 *
 * @author 姚泰然
 * @date 2025-12-23 14:28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TenantInsertVO extends TenantUpdateVO {

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
