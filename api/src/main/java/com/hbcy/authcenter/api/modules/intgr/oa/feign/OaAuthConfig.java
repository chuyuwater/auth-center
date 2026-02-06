package com.hbcy.authcenter.api.modules.intgr.oa.feign;

import feign.Logger;
import org.springframework.context.annotation.Bean;

/**
 * @author 姚泰然
 * @date 2026-02-06 14:15
 */
public class OaAuthConfig {
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.NONE;
    }
}
