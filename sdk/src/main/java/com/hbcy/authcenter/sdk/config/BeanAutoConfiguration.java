package com.hbcy.authcenter.sdk.config;

import com.hbcy.authcenter.sdk.filter.FeignHeaderInterceptor;
import com.hbcy.authcenter.sdk.filter.HeaderContextFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * @author 姚泰然
 * @date 2025-12-23 13:08
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class BeanAutoConfiguration {
    @Bean
    public FilterRegistrationBean<HeaderContextFilter> headerFilter() {
        FilterRegistrationBean<HeaderContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HeaderContextFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FeignHeaderInterceptor feignHeaderInterceptor() {
        return new FeignHeaderInterceptor();
    }
}
