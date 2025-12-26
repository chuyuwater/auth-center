package com.hbcy.authcenter.api.modules.core.user.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 切换主职组织
 *
 * @author 姚泰然
 * @date 2025-12-26 19:41
 */
@Data
public class SwitchDefaultOrgVO {
    /**
     * 组织id
     */
    @NotBlank(message = "组织id不能为空")
    private String orgId;
    /**
     * 用户id
     */
    @NotBlank(message = "用户id不能为空")
    private String userId;
}
