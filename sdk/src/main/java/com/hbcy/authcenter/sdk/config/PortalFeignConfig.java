package com.hbcy.authcenter.sdk.config;

import com.hbcy.authcenter.sdk.filter.FeignHeaderInterceptor;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * @author 姚泰然
 * @date 2026-01-20 15:16
 */
public class PortalFeignConfig {
    @Bean
    private RequestInterceptor feignHeaderInterceptor() {
        return new FeignHeaderInterceptor();
    }
}
