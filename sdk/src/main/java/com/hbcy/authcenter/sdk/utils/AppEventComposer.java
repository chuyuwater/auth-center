package com.hbcy.authcenter.sdk.utils;

import com.hbcy.authcenter.global.dto.AppEventOutDTO;
import com.hbcy.authcenter.global.dto.EventMeta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 建议应用自己写一个bean，包装默认参数（如topic）
 *
 * @author 姚泰然
 * @date 2026-01-08 12:50
 */
@Component
public class AppEventComposer {
    @Value("${spring.application.name}")
    private String serviceName;

    public AppEventOutDTO createAppEvent(String topic, String shardingKey, String eventCode,
                                         long eventVersion, Object payload) {
        AppEventOutDTO event = new AppEventOutDTO();
        EventMeta meta = new EventMeta()
                .setEventCode(UUID.randomUUID().toString())
                .setTimestamp(System.currentTimeMillis())
                .setSource(serviceName)
                .setTopic(topic)
                .setShardingKey(shardingKey)
                .setEventVersion(eventVersion)
                .setEventCode(eventCode)
                .setTraceId(UserContextUtils.getTraceId());
        event.setMeta(meta);
        event.setPayload(payload);
        return event;
    }

    public AppEventOutDTO createAppEvent(String topic, String eventCode, Object payload) {
        return createAppEvent(topic, null, eventCode, 1, payload);
    }
}
