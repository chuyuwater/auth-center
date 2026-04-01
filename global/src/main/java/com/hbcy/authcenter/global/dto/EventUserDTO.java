package com.hbcy.authcenter.global.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 对外消息中的用户简略信息
 * @author 姚泰然
 * @date 2026-04-01 15:13
 */
@Data
@Accessors(chain = true)
public class EventUserDTO {
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 租户ID
     */
    private String tenantId;
}
