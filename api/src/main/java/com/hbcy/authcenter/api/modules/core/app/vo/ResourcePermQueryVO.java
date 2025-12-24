package com.hbcy.authcenter.api.modules.core.app.vo;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2025-12-24
 */
@Data
public class ResourcePermQueryVO {
    /**
     * 关联的资源id
     */
    private String resId;
    /**
     * 应用id
     */
    private String appId;
    /**
     * 权限码
     */
    private String permCode;
}
