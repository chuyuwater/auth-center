package com.hbcy.authcenter.sdk.filter;

import com.hbcy.authcenter.sdk.annotation.EnableHeaderPassthrough;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.lang.reflect.Method;
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
        //检查方法或类上是否有注解
        Method method = template.methodMetadata().method();
        Class<?> targetClass = method.getDeclaringClass();

        if (method.isAnnotationPresent(EnableHeaderPassthrough.class) ||
                targetClass.isAnnotationPresent(EnableHeaderPassthrough.class)) {

            Map<String, String> headers = UserContextUtils.getHeaders();
            if (headers != null) {
                headers.forEach((key, value) -> {
                    if (value != null) template.header(key, value);
                });
            }
        }
    }
}
