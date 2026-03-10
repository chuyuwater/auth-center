package com.hbcy.authcenter.api.modules.core.app.dto;

import lombok.Data;

/**
 * 权限点列表
 *
 * @author 姚泰然
 * @date 2025-12-28 14:16
 */
@Data
public class ResPermDTO {
    /**
     * 权限ID
     */
    private String id;
    /**
     * 应用ID
     */
    private String appId;
    /**
     * 资源ID
     */
    private String resId;
    /**
     * 权限名称
     */
    private String permName;
    /**
     * 权限编码
     */
    private String permCode;
}
