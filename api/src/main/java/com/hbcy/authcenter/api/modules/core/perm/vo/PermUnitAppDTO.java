package com.hbcy.authcenter.api.modules.core.perm.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 权限单元应用DTO
 *
 * @author 姚泰然
 * @date 2025-12-28 12:04
 */
@Data
@Accessors(chain = true)
public class PermUnitAppDTO {
    /**
     * 应用id
     */
    private String appId;
    /**
     * 应用图标
     */
    private String appIcon;
    /**
     * 应用名
     */
    private String appName;
    /**
     * 应用简介
     */
    private String appMemo;
    /**
     * 资源是否已封装
     */
    private boolean packed;
    /**
     * 租户侧应用授权是否禁用，0-启用，1-禁用
     */
    private Integer forbidden;
}
