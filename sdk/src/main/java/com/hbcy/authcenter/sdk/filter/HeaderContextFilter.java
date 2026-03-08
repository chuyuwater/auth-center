package com.hbcy.authcenter.sdk.filter;

import com.hbcy.authcenter.global.constants.AuthConstants;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 姚泰然
 * @date 2025-12-23 12:53
 */
public class HeaderContextFilter extends OncePerRequestFilter {

    private static String extractTraceId(String traceParent) {
        if (traceParent != null && traceParent.length() >= 55) {
            // 按照横杠拆分，或者根据固定位置截取
            // 格式：00-traceId(32)-parentId(16)-flags(2)
            String[] parts = traceParent.split("-");
            if (parts.length >= 4) {
                return parts[1];
            }
        }
        return "";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put(AuthConstants.HEADER_USER_ID, request.getHeader(AuthConstants.HEADER_USER_ID));
            headers.put(AuthConstants.HEADER_USER_IP, request.getHeader(AuthConstants.HEADER_USER_IP));
            headers.put(AuthConstants.HEADER_USER_NAME, request.getHeader(AuthConstants.HEADER_USER_NAME));
            headers.put(AuthConstants.HEADER_APP_ID, request.getHeader(AuthConstants.HEADER_APP_ID));
            headers.put(AuthConstants.HEADER_TENANT_ID, request.getHeader(AuthConstants.HEADER_TENANT_ID));
            headers.put(AuthConstants.HEADER_ADMIN_FLAG, request.getHeader(AuthConstants.HEADER_ADMIN_FLAG));
            headers.put(AuthConstants.HEADER_ORG_ID, request.getHeader(AuthConstants.HEADER_ORG_ID));
            if (StringUtils.isNotBlank(request.getHeader(AuthConstants.HEADER_TRACE_PARENT))) {
                //如果使用sdk的微服务用了open-telemetry，也可以自己提取traceId，否则可以从这个header里直接获取
                headers.put(AuthConstants.HEADER_TRACE_ID,
                        extractTraceId(request.getHeader(AuthConstants.HEADER_TRACE_PARENT)));
            } else if (StringUtils.isNotBlank(request.getHeader(AuthConstants.HEADER_TRACE_ID))) {
                //也支持非open-telemetry环境时，手动注入的traceId
                headers.put(AuthConstants.HEADER_TRACE_ID, request.getHeader(AuthConstants.HEADER_TRACE_ID));
            }
            MDC.put("appId", request.getHeader(AuthConstants.HEADER_APP_ID));
            MDC.put("userId", request.getHeader(AuthConstants.HEADER_USER_ID));
            MDC.put("tenantId", request.getHeader(AuthConstants.HEADER_TENANT_ID));
            MDC.put("orgId", request.getHeader(AuthConstants.HEADER_ORG_ID));
            UserContextUtils.setHeaders(headers);
            filterChain.doFilter(request, response);
        } finally {
            UserContextUtils.clear();
            MDC.clear();
        }
    }
}
