package com.hbcy.authcenter.api.modules.core.perm.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 姚泰然
 * @date 2025-12-27 20:34
 */
@Data
@NoArgsConstructor
@TableName(value = "perm_unit")
public class PermUnit {
    /**
     * 规则生成名称
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 名称
     */
    @TableField(value = "name_cn")
    private String nameCn;

    /**
     * 授权模型，0-RBAC，1-ABAC
     */
    @TableField(value = "policy_model")
    private Integer policyModel;

    /**
     * 归属分组
     */
    @TableField(value = "belong_to")
    private String belongTo;

    /**
     * 说明
     */
    @TableField(value = "memo")
    private String memo;

    @TableField(value = "forbidden")
    private Integer forbidden;

    /**
     * 系统创建的数据，禁止删除
     */
    @TableField(value = "sys_protect")
    private Integer sysProtect;

    @TableField(value = "tenant_id")
    private String tenantId;

    @TableField(value = "create_user")
    private String createUser;

    @TableField(value = "update_user")
    private String updateUser;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public static final String COL_ID = "id";

    public static final String COL_NAME_CN = "name_cn";

    public static final String COL_POLICY_MODEL = "policy_model";

    public static final String COL_BELONG_TO = "belong_to";

    public static final String COL_MEMO = "memo";

    public static final String COL_FORBIDDEN = "forbidden";

    public static final String COL_SYS_PROTECT = "sys_protect";

    public static final String COL_TENANT_ID = "tenant_id";

    public static final String COL_CREATE_USER = "create_user";

    public static final String COL_UPDATE_USER = "update_user";

    public static final String COL_CREATE_TIME = "create_time";

    public static final String COL_UPDATE_TIME = "update_time";
}