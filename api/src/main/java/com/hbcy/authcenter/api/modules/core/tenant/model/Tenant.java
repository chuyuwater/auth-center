package com.hbcy.authcenter.api.modules.core.tenant.model;

import com.baomidou.mybatisplus.annotation.*;
import com.hbcy.authcenter.api.common.bean.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-25 15:38
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@TableName(value = "tenant")
public class Tenant extends BaseEntity {
    public static final String COL_ID = "id";
    public static final String COL_NAME_CN = "name_cn";
    public static final String COL_SHORT_NAME = "short_name";
    public static final String COL_LOGO = "logo";
    public static final String COL_MEMO = "memo";
    public static final String COL_ADMIN_ID = "admin_id";
    public static final String COL_FORBIDDEN = "forbidden";
    public static final String COL_CONTACT_USER = "contact_user";
    public static final String COL_CONTACT_PHONE = "contact_phone";
    public static final String COL_DELETE_TIME = "delete_time";
    public static final String COL_CREATE_USER = "create_user";
    public static final String COL_UPDATE_USER = "update_user";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_UPDATE_TIME = "update_time";
    /**
     * 算法生成id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    /**
     * 中文名称
     */
    @TableField(value = "name_cn")
    private String nameCn;
    /**
     * 简称
     */
    @TableField(value = "short_name")
    private String shortName;
    /**
     * logo url
     */
    @TableField(value = "logo")
    private String logo;
    /**
     * 备注信息
     */
    @TableField(value = "memo")
    private String memo;
    /**
     * 0-启用，1-禁用
     */
    @TableField(value = "forbidden")
    private Integer forbidden;
    /**
     * 联系人
     */
    @TableField(value = "contact_user")
    private String contactUser;
    /**
     * 联系电话
     */
    @TableField(value = "contact_phone")
    private String contactPhone;
    /**
     * 删除时间戳标记
     */
    @TableField(value = "delete_time")
    @TableLogic(value = "0", delval = "-1")
    private Long deleteTime;

    /**
     * 租户管理员id
     */
    @TableField(value = "admin_id")
    private String adminId;
    /**
     * 创建者
     */
    @TableField(value = "create_user")
    private String createUser;
    @TableField(value = "update_user")
    private String updateUser;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}