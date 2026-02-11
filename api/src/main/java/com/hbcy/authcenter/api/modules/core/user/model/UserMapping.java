package com.hbcy.authcenter.api.modules.core.user.model;

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
 * @date 2026-02-11 16:16
 */
/**
 * 用户映射
 */
@Data
@NoArgsConstructor
@TableName(value = "sys_user_mapping")
public class UserMapping {
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 我方用户id
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 第三方平台，字典项
     */
    @TableField(value = "third_party")
    private String thirdParty;

    /**
     * 第三方平台id
     */
    @TableField(value = "third_id")
    private String thirdId;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public static final String COL_ID = "id";

    public static final String COL_USER_ID = "user_id";

    public static final String COL_THIRD_PARTY = "third_party";

    public static final String COL_THIRD_ID = "third_id";

    public static final String COL_CREATE_TIME = "create_time";
}