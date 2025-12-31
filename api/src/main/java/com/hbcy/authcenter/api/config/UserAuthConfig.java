package com.hbcy.authcenter.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 用户会话配置
 *
 * @author 姚泰然
 * @date 2025-12-31 09:04
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "user.auth")
@Data
public class UserAuthConfig {
    /**
     * Token最大过期时间
     */
    private Duration tokenExpire = Duration.ofDays(7);

    /**
     * Token多久无活跃自动过期
     */
    private Duration tokenInactive = Duration.ofDays(1);

    /**
     * 登录失败重试次数
     */
    private Integer maxRetry = 5;

    /**
     * 登录失败重试间隔时间
     */
    private Duration retryTime = Duration.ofMinutes(1);

    /**
     * 达到最大失败之后锁定时间
     */
    private Duration lockTime = Duration.ofMinutes(5);
    /**
     * 权限缓存时间
     */
    private Duration permExpire = Duration.ofMinutes(30);
}
