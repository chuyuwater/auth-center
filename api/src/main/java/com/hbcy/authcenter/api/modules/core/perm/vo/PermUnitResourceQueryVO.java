package com.hbcy.authcenter.api.modules.core.perm.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 权限单元资源查询VO
 *
 * @author 姚泰然
 * @date 2025-12-28 12:09
 */
@Data
public class PermUnitResourceQueryVO {
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
     * 是否仅需要已封装的资源树
     * 为false时，构建全部的资源树，并标记是否已勾选
     * 为true时，仅返回已封装的权限，未被封装的节点会被移除
     */
    private Boolean onlyPacked = false;
}
