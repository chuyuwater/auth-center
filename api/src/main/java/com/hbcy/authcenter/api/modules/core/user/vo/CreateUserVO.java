package com.hbcy.authcenter.api.modules.core.user.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建用户需要的数据
 *
 * @author 姚泰然
 * @date 2025-12-26 08:55
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CreateUserVO extends UpdateUserVO {
    /**
     * 创建时归属组织，即为其默认主职组织
     */
    @NotBlank(message = "归属组织不能为空")
    private String orgId;
}
