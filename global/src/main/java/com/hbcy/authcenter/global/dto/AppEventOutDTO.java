package com.hbcy.authcenter.global.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 应用事件通知，发送时用
 * 不同应用（业务域）使用不同的kafka topic
 *
 * @author 姚泰然
 * @date 2025-12-31 14:11
 */
@Data
@Accessors(chain = true)
public class AppEventOutDTO {
    /**
     * 事件元数据
     */
    private EventMeta meta;
    /**
     * 事件详情
     */
    private Object payload;
}
