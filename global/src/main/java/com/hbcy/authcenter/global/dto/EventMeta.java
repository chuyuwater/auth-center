package com.hbcy.authcenter.global.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 姚泰然
 * @date 2026-01-08 12:45
 */
@Data
@Accessors(chain = true)
public class EventMeta {
    /**
     * 事件id，避免重复消费
     */
    private String eventId;
    /**
     * topic名字
     */
    private String topic;
    /**
     * 事件编码
     */
    private String eventCode;
    /**
     * 事件版本
     */
    private long eventVersion = 1;
    /**
     * 时间戳
     */
    private long timestamp;
    /**
     * 事件源，一般是微服务的名字
     */
    private String source;
    /**
     * 如果事件需要保持有序，此处传入分片key
     */
    private String shardingKey = "";
    /**
     * 事件触发的traceId
     */
    private String traceId;
}
