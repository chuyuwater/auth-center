package com.hbcy.authcenter.api.modules.core.app.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源权限创建VO
 *
 * @author 姚泰然
 * @date 2025-12-24
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ResourcePermCreateVO extends ResourcePermUpdateVO {
    /**
     * 权限点id
     */
    private String id;
}
