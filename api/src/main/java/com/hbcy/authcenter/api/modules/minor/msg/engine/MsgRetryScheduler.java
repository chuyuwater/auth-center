package com.hbcy.authcenter.api.modules.minor.msg.engine;

import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgLogMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息重试调度器
 * 每20秒轮询失败且未超过最大重试次数的消息进行重试
 */
@Service
@Slf4j
@EnableScheduling
public class MsgRetryScheduler {
    @Resource
    private MsgLogMapper msgLogMapper;
    @Resource
    private MsgSendEngine msgSendEngine;

    @Scheduled(fixedDelay = 20000)
    public void retry() {
        List<MsgLog> pending = msgLogMapper.selectRetryPending(5, 20);
        for (MsgLog msgLog : pending) {
            try {
                msgSendEngine.retrySend(msgLog);
            } catch (Exception e) {
                log.error("消息重试失败: logId={}", msgLog.getId(), e);
            }
        }
    }
}
