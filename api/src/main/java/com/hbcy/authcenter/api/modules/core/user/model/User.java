package com.hbcy.authcenter.api.modules.core.user.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import com.hbcy.common.db.dictvalue.DictInject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-26 08:54
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@TableName(value = "sys_user")
public class User extends BaseEntity {
    public static final String COL_ID = "id";
    public static final String COL_PHONE = "phone";
    public static final String COL_ACCOUNT = "account";
    public static final String COL_REAL_NAME = "real_name";
    public static final String COL_PASSWD = "passwd";
    public static final String COL_EMAIL = "email";
    public static final String COL_AVATAR = "avatar";
    public static final String COL_FORBIDDEN = "forbidden";
    public static final String COL_WECOM_ID = "wecom_id";
    public static final String COL_SRC_TYPE = "src_type";
    public static final String COL_SRC_ID = "src_id";
    public static final String COL_EMPLOYEE_TYPE = "employee_type";
    public static final String COL_PASSWD_EXPIRE = "passwd_expire";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_LAST_LOGIN = "last_login";
    public static final String COL_DELETE_TIME = "delete_time";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    /**
     * 用户id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    /**
     * 手机号
     */
    @TableField(value = "phone")
    private String phone;
    /**
     * 登录账号
     */
    @TableField(value = "account")
    private String account;
    /**
     * 姓名
     */
    @TableField(value = "real_name")
    private String realName;
    /**
     * bcrypt加密的密码
     */
    @TableField(value = "passwd")
    private String passwd;
    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;
    /**
     * 头像
     */
    @TableField(value = "avatar")
    private String avatar;
    @TableField(value = "forbidden")
    private Integer forbidden;
    /**
     * 企业微信id
     */
    @TableField(value = "wecom_id")
    private String wecomId;
    /**
     * 账号来源，0-自建，1-OA同步
     */
    @TableField(value = "src_type")
    private Integer srcType;
    /**
     * 源系统id
     */
    @TableField(value = "src_id")
    private String srcId;
    /**
     * 用工类型，0-自有，1-派遣，2-外包，3-外部公司人员
     * 允许为null
     */
    @TableField(value = "employee_type")
    @DictInject(value = "EMPLOYEE_TYPE")
    private Integer employeeType;
    /**
     * 密码过期时间，null标识永不过期
     */
    @TableField(value = "passwd_expire")
    private LocalDateTime passwdExpire;
    /**
     * 最后登录时间
     */
    @TableField(value = "last_login")
    private LocalDateTime lastLogin;
    /**
     * 账号所属租户
     */
    @TableField(value = "tenant_id")
    private String tenantId;
    @TableField(value = "delete_time")
    private Long deleteTime;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(value = "create_user")
    private String createUser;
    @TableField(value = "update_user")
    private String updateUser;
}