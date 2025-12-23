package com.hbcy.authcenter.modules.core.tenant.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 姚泰然
 * @date 2025-12-23 09:49
 */
@Data
@NoArgsConstructor
@TableName(value = "tenant")
public class Tenant {
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
    private Long deleteTime;

    /**
     * 创建者
     */
    @TableField(value = "create_user")
    private String createUser;

    @TableField(value = "update_user")
    private String updateUser;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    public static final String COL_ID = "id";

    public static final String COL_NAME_CN = "name_cn";

    public static final String COL_LOGO = "logo";

    public static final String COL_MEMO = "memo";

    public static final String COL_FORBIDDEN = "forbidden";

    public static final String COL_CONTACT_USER = "contact_user";

    public static final String COL_CONTACT_PHONE = "contact_phone";

    public static final String COL_DELETE_TIME = "delete_time";

    public static final String COL_CREATE_USER = "create_user";

    public static final String COL_UPDATE_USER = "update_user";

    public static final String COL_CREATE_TIME = "create_time";

    public static final String COL_UPDATE_TIME = "update_time";
}