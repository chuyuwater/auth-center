package com.hbcy.authcenter.sdk.config;

import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskDecorator;

import java.util.concurrent.Executor;

/**
 * 将用户上下文注入线程池
 *
 * @author 姚泰然
 * @date 2025-12-23 14:00
 */
@AutoConfiguration
public class TtlBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 拦截所有的 Executor 类型（包括 ThreadPoolTaskExecutor）
        if (bean instanceof Executor executor && !(bean instanceof TtlEnhanced)) {
            // 使用 TTL 提供的包装工具
            return TtlExecutors.getTtlExecutor(executor);
        }
        return bean;
    }

    /**
     * 自动包装@Async异步任务，防止Header上下文丢失
     */
    @Bean
    public TaskDecorator ttlTaskDecorator() {
        return TtlRunnable::get;
    }
}