package com.hbcy.authcenter.api.modules.core.perm.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限单元关联的用户
 *
 * @author 姚泰然
 * @date 2025-12-28 20:08
 */
@Data
public class UnitUserDTO {
    /**
     * 关联关系id
     */
    private String grantId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 用户的名字
     */
    private String userName;
    /**
     * 用户账号
     */
    private String account;
    /**
     * 组织id
     */
    private String orgId;
    /**
     * 组织名称
     */
    private String orgName;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 创建人id
     */
    private String createUser;
    /**
     * 创建人名称
     */
    private String createUserName;
}
