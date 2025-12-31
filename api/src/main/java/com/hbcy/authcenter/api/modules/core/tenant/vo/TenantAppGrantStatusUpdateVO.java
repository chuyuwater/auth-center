package com.hbcy.authcenter.api.modules.core.tenant.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * 租户应用授权状态更新VO
 *
 * @author 姚泰然
 * @date 2025-12-23 17:37
 */
@Data
public class TenantAppGrantStatusUpdateVO {
    /**
     * 绑定关系ID
     */
    @NotBlank(message = "授权ID不能为空")
    private String grantId;
    /**
     * 是否禁用，0-未禁用，1-已禁用
     */
    @Range(min = 0, max = 1, message = "禁用状态只能为0或1")
    private Integer forbidden = 0;
}
