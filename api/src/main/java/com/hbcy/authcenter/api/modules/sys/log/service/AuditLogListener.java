package com.hbcy.authcenter.api.modules.sys.log.service;

import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.modules.sys.log.model.AuditLog;
import com.hbcy.authcenter.gateway.constants.GatewayConstants;
import jakarta.annotation.Resource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 记录kafka传过来的审计日志
 *
 * @author 姚泰然
 * @date 2026-01-13 13:59
 */
@Component
public class AuditLogListener {
    @Resource
    private IAuditLogService auditLogService;

    @KafkaListener(topics = GatewayConstants.KAFKA_TOPIC_AUDIT_LOG, groupId = G.SERVICE_NAME)
    public void saveLog(List<ConsumerRecord<String, AuditLog>> records, Acknowledgment ack) {
        auditLogService.save(records.stream().map(ConsumerRecord::value).toList());
        ack.acknowledge();
    }
}
