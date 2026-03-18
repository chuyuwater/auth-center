package com.hbcy.authcenter.api.modules.core.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户管理列表/详情页返回数据
 *
 * @author 姚泰然
 * @date 2025-12-26 13:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryResultDTO extends UserDTO {
    /**
     * 用户任职概况
     */
    private List<UserOrgDTO> orgList;
    /**
     * 是否租户管理员
     */
    private boolean tenantAdmin;
    /**
     * 创建人名称
     */
    private String createUserName;
    /**
     * 修改人名称
     */
    private String updateUserName;
}
