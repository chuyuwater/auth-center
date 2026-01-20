package com.hbcy.authcenter.sdk.filter;

import com.hbcy.authcenter.global.constants.AuthConstants;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.Map;

/**
 * feign请求透传客户端请求的Header
 * 请使用@EnableHeaderPassthrough对feign接口类进行注解
 *
 * @author 姚泰然
 * @date 2025-12-23 13:08
 */
public class FeignHeaderInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        Map<String, String> headers = UserContextUtils.getHeaders();
        if (headers != null) {
            //标记SDK版本
            headers.put(AuthConstants.HEADER_SDK_VERSION, AuthConstants.SDK_VERSION);
            headers.forEach((key, value) -> {
                if (value != null) template.header(key, value);
            });
        }
    }
}
