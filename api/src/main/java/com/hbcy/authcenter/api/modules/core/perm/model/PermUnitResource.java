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
@TableName(value = "perm_unit_resource")
public class PermUnitResource {
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 权限单元id
     */
    @TableField(value = "unit_id")
    private String unitId;

    /**
     * 应用id
     */
    @TableField(value = "app_id")
    private String appId;

    /**
     * 权限码id
     */
    @TableField(value = "code_id")
    private String codeId;

    @TableField(value = "create_user")
    private String createUser;

    @TableField(value = "update_user")
    private String updateUser;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public static final String COL_ID = "id";

    public static final String COL_UNIT_ID = "unit_id";

    public static final String COL_APP_ID = "app_id";

    public static final String COL_CODE_ID = "code_id";

    public static final String COL_CREATE_USER = "create_user";

    public static final String COL_UPDATE_USER = "update_user";

    public static final String COL_CREATE_TIME = "create_time";

    public static final String COL_UPDATE_TIME = "update_time";
}