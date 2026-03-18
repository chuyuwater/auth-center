package com.hbcy.authcenter.sdk.feign.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情
 * @author 姚泰然
 * @date 2026-03-18 12:24
 */
@Data
public class UserDetailDTO {
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
    private Integer employeeType;
    /**
     * 用工类型名称
     */
    private String employeeTypeName;
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
    /**
     * 用户任职概况
     */
    private List<UserOrgDetail> orgList;
}
