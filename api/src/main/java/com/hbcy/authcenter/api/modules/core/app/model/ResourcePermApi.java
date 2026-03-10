package com.hbcy.authcenter.api.modules.core.app.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限对应api
 * @author 姚泰然
 * @date 2026-03-10 08:46
 */

@Data
@NoArgsConstructor
@TableName(value = "resource_perm_api")
public class ResourcePermApi {
    public static final String COL_ID = "id";
    public static final String COL_PERM_ID = "perm_id";
    public static final String COL_API_METHOD = "api_method";
    public static final String COL_API_PATH = "api_path";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_CREATE_USER = "create_user";
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @TableField(value = "perm_id")
    private String permId;
    /**
     * 0-GET,1-POST,2-PUT,3-DELETE
     */
    @TableField(value = "api_method")
    private Integer apiMethod;
    @TableField(value = "api_path")
    private String apiPath;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "create_user")
    private String createUser;
}