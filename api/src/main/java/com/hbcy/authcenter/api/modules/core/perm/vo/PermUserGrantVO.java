package com.hbcy.authcenter.api.modules.core.perm.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author 姚泰然
 * @date 2025-12-28 19:30
 */
@Data
public class PermUserGrantVO {
    @NotBlank(message = "用户id不能为空")
    private String userId;
    @NotBlank(message = "组织id不能为空")
    private String orgId;
}
