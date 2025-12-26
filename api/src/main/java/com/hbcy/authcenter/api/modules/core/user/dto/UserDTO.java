package com.hbcy.authcenter.api.modules.core.user.dto;

import com.hbcy.common.db.convertor.UserNameFill;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-26 13:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserDTO extends UserSimpleDTO {
    private String phone;
    private String email;
    private Integer forbidden;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @UserNameFill
    private String createUser;
    @UserNameFill
    private String updateUser;
    List<>
}
