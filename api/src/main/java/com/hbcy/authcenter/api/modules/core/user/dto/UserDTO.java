package com.hbcy.authcenter.api.modules.core.user.dto;

import com.hbcy.common.db.dictvalue.DictInject;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考虑隐私设置，大部分情况下只显示这些信息
 *
 * @author 姚泰然
 * @date 2025-12-26 13:45
 */
@Data
public class UserDTO {
    /**
     * 用户id
     */
    private String id;
    /**
     * 姓名
     */
    private String realName;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 账号
     */
    private String account;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 是否禁用
     */
    private Integer forbidden;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 用工类型，字典EMPLOYEE_TYPE
     */
    @DictInject(value = "EMPLOYEE_TYPE")
    private Integer employeeType;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建人
     */
    private String createUser;
    /**
     * 修改人
     */
    private String updateUser;
}
