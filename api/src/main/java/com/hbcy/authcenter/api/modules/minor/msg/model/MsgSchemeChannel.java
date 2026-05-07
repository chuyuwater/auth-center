package com.hbcy.authcenter.api.modules.minor.msg.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 发送方案渠道关联
 */
@Data
@NoArgsConstructor
@TableName(value = "msg_scheme_channel")
@Accessors(chain = true)
public class MsgSchemeChannel {
    public static final String COL_ID = "id";
    public static final String COL_SCHEME_ID = "scheme_id";
    public static final String COL_CHANNEL_ID = "channel_id";
    public static final String COL_TENANT_ID = "tenant_id";
    public static final String COL_CREATE_TIME = "create_time";

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @TableField(value = "scheme_id")
    private String schemeId;
    @TableField(value = "channel_id")
    private String channelId;
    @TableField(value = "tenant_id")
    private String tenantId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
