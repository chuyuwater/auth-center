package com.hbcy.authcenter.api.modules.core.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 列表/详情页返回数据
 *
 * @author 姚泰然
 * @date 2025-12-26 13:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryResultDTO extends UserDTO {
    List<UserOrgDTO> orgList;
    private String createUserName;
    private String updateUserName;
}
