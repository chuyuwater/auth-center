package com.hbcy.authcenter.api.modules.core.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 列表/详情页返回数据
 *
 * @author 姚泰然
 * @date 2025-12-26 13:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserDTO extends UserSimpleDTO {
    List<UserOrgSimpleDTO> orgList;
    private String phone;
    private String email;
    private Integer forbidden;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createUser;
    private String createUserName;
    private String updateUser;
    private String updateUserName;
}
