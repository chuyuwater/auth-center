package com.hbcy.authcenter.api.modules.core.app.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 租户自行绑定组织树
 *
 * @author 姚泰然
 * @date 2025-12-24 09:09
 */
@Data
public class BindOrgTreeVO {
    /**
     * 应用ID
     */
    @NotBlank(message = "应用ID不能为空")
    private String appId;
    /**
     * 组织树ID
     */
    @NotBlank(message = "组织树根节点ID不能为空")
    private String orgRootId;
}
