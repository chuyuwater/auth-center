package com.hbcy.authcenter.api.modules.core.perm.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-02-24 10:33
 */
@Data
public class UserUnitDTO {
    /**
     * 授权id
     */
    private String grantId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 角色id
     */
    private String unitId;
    /**
     * 角色名称
     */
    private String unitName;
    /**
     * 组织id
     */
    private String orgId;
    /**
     * 组织名称
     */
    private String orgName;
    /**
     * 授权人id
     */
    private String createUser;
    /**
     * 授权时间
     */
    private String createTime;
}
