package com.hbcy.authcenter.api.modules.core.perm.vo;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-01-13 17:16
 */
@Data
public class UserQueryByPermVO {
    /**
     * 需要筛选权限的应用
     */
    private String appId;
    /**
     * 需要的权限编码
     */
    private String permCode;
    /**
     * 用户的组织id
     */
    private String orgId;
    /**
     * orgId对应的查询范围
     * 0-本下，1-本级，2-下级
     */
    private Integer orgRange;
}
