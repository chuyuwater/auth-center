package com.hbcy.authcenter.api.common.bean;

import com.hbcy.authcenter.global.constants.EventConstants;
import com.hbcy.authcenter.global.dto.AppEventOutDTO;
import com.hbcy.authcenter.sdk.utils.AppEventComposer;
import jakarta.annotation.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 发布应用事件到kafka
 *
 * @author 姚泰然
 * @date 2025-12-31 14:24
 */
@Service
public class EventDispatcher {
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Resource
    private AppEventComposer appEventComposer;

    /**
     * 发布应用事件
     * 乱序、事件版本为1
     *
     * @param eventCode 事件编码
     * @param payload   事件详情
     */
    public void dispatch(String eventCode, Object payload) {
        AppEventOutDTO event = appEventComposer.createAppEvent(
                EventConstants.KAFKA_TOPIC_PORTAL_EVENT, eventCode, payload);
        kafkaTemplate.send(EventConstants.KAFKA_TOPIC_PORTAL_EVENT, event);
    }

    /**
     * 发布应用事件
     * 以shardingKey为分区键排序，应用版本1
     *
     * @param shardingKey 排序值
     * @param eventCode   事件编码
     * @param payload     事件详情
     */
    public void dispatch(String shardingKey, String eventCode, Object payload) {
        AppEventOutDTO event = appEventComposer.createAppEvent(
                EventConstants.KAFKA_TOPIC_PORTAL_EVENT, shardingKey, eventCode, 1, payload);
        kafkaTemplate.send(EventConstants.KAFKA_TOPIC_PORTAL_EVENT, shardingKey, event);
    }

    /**
     * 发布应用事件
     * 以shardingKey为分区键排序，应用版本指定
     *
     * @param shardingKey  排序值
     * @param eventCode    事件编码
     * @param eventVersion 事件版本
     * @param payload      事件详情
     */
    public void dispatch(String shardingKey, String eventCode, int eventVersion, Object payload) {
        AppEventOutDTO event = appEventComposer.createAppEvent(
                EventConstants.KAFKA_TOPIC_PORTAL_EVENT, shardingKey, eventCode, eventVersion, payload);
        kafkaTemplate.send(EventConstants.KAFKA_TOPIC_PORTAL_EVENT, shardingKey, event);
    }
}
