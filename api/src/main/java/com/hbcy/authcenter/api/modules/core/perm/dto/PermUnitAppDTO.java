package com.hbcy.authcenter.api.modules.core.perm.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-02-11 14:44
 */
@Data
public class PermUnitAppDTO {
    /**
     * 权限单元ID
     */
    private String unitId;
    /**
     * 应用id
     */
    private String appId;
    /**
     * 应用名称
     */
    private String appName;
}
