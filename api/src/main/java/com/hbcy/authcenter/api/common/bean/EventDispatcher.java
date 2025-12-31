package com.hbcy.authcenter.api.common.bean;

import com.hbcy.authcenter.sdk.bean.AppEventOutDTO;
import com.hbcy.authcenter.sdk.constants.EventConstants;
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

    public void dispatch(AppEventOutDTO event) {
        kafkaTemplate.send(EventConstants.KAFKA_TOPIC, event.getAppId(), event);
    }
}
