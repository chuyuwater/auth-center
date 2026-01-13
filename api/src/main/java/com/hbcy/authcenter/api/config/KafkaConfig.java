package com.hbcy.authcenter.api.config;

import com.google.common.collect.ImmutableMap;
import com.hbcy.authcenter.api.modules.sys.log.model.AuditLog;
import com.hbcy.authcenter.global.dto.AppEventInDTO;
import com.hbcy.authcenter.global.dto.AppEventOutDTO;
import com.hbcy.common.kafka.CustomJsonDeserializer;
import com.hbcy.common.kafka.CustomJsonSerializer;
import com.hbcy.common.kafka.KafkaUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 姚泰然
 * @date 2025-12-31 17:38
 */
@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig implements KafkaListenerConfigurer {
    /**
     * 序列化key对应的type，放置在header里，与平台侧消费端要一致
     */
    private final Map<String, Class<?>> produceTypes = ImmutableMap.<String, Class<?>>builder()
            .put("event", AppEventOutDTO.class)
            .build();
    private final Map<String, Class<?>> consumeTypes = ImmutableMap.<String, Class<?>>builder()
            .put("audit", AuditLog.class)
            .put("event", AppEventInDTO.class)
            .build();

    @Resource
    private LocalValidatorFactoryBean validator;

    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;
    @Value("${spring.kafka.consumer.group-id:portal-auth-center}")
    private String groupName;

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        registrar.setValidator(this.validator);
    }

    @Bean
    public DefaultKafkaHeaderMapper defaultKafkaHeaderMapper() {
        DefaultKafkaHeaderMapper defaultKafkaHeaderMapper = new DefaultKafkaHeaderMapper();
        defaultKafkaHeaderMapper.addTrustedPackages("*");
        return defaultKafkaHeaderMapper;
    }

    /**
     * 生产者配置
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CustomJsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        props.put(ProducerConfig.RETRIES_CONFIG, 1);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        props.put(JsonSerializer.TYPE_MAPPINGS, KafkaUtils.genTypeMappingString(produceTypes));
        return props;
    }

    /**
     * 消费者配置
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomJsonDeserializer.class);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 600000);
        props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 2000);
        //避免频繁重平衡
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 120000);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.TYPE_MAPPINGS, KafkaUtils.genTypeMappingString(consumeTypes));
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return props;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ConsumerFactory<?, ?> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(), true);
    }
}
