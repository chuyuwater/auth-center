package com.hbcy.authcenter.api.modules.intgr.oa.feign;

import com.hbcy.authcenter.api.modules.intgr.oa.dto.OaAccessDataDTO;
import com.hbcy.authcenter.api.modules.intgr.oa.service.EcologyService;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * @author 姚泰然
 * @date 2026-01-20 14:52
 */
public class OaReqConfig {
    /**
     * 调用OA API时，使用的用户id，需要有相关权限
     */
    @Value("${oa.access-user:370}")
    private String userId;

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor requestInterceptor(ObjectProvider<EcologyService> ecologyServiceProvider) {
        return requestTemplate -> {
            EcologyService service = ecologyServiceProvider.getIfAvailable();
            if (service != null) {
                OaAccessDataDTO dto = service.getOaAccessHeaders(userId);
                requestTemplate.header("appid", dto.getAppid());
                requestTemplate.header("userid", dto.getUserid());
                requestTemplate.header("token", dto.getToken());
            }
        };
    }
}
