package com.hbcy.authcenter.api.modules.core.perm.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

/**
 * 权限单元资源保存VO
 *
 * @author 姚泰然
 * @date 2025-12-28
 */
@Data
public class PermUnitResourceSaveVO {
    /**
     * 权限单元id
     */
    @NotBlank(message = "权限单元id不能为空")
    private String unitId;
    /**
     * 应用id
     */
    @NotBlank(message = "应用id不能为空")
    private String appId;
    /**
     * 权限码
     */
    @NotEmpty(message = "权限id不能为空")
    private Set<String> permIds;
}
