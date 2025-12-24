package com.hbcy.authcenter.api.modules.core.app.vo;

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
    private String appId;
    /**
     * 组织树ID
     */
    private String orgTreeId;
}
