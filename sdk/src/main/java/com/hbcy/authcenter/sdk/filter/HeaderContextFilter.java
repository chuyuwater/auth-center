package com.hbcy.authcenter.sdk.filter;

import com.hbcy.authcenter.sdk.constants.AuthConstants;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put(AuthConstants.HEADER_USER_ID, request.getHeader(AuthConstants.HEADER_USER_ID));
            headers.put(AuthConstants.HEADER_APP_ID, request.getHeader(AuthConstants.HEADER_APP_ID));
            headers.put(AuthConstants.HEADER_TENANT_ID, request.getHeader(AuthConstants.HEADER_TENANT_ID));
            headers.put(AuthConstants.HEADER_ADMIN_FLAG, request.getHeader(AuthConstants.HEADER_ADMIN_FLAG));
            MDC.put("appId", request.getHeader(AuthConstants.HEADER_APP_ID));
            MDC.put("userId", request.getHeader(AuthConstants.HEADER_USER_ID));
            MDC.put("tenantId", request.getHeader(AuthConstants.HEADER_TENANT_ID));
            MDC.put("orgId", request.getHeader(AuthConstants.HEADER_ORG_ID));
            if (request.getHeader(AuthConstants.HEADER_TRACE_ID) != null) {
                headers.put(AuthConstants.HEADER_TRACE_ID, request.getHeader(AuthConstants.HEADER_TRACE_ID));
                //注入traceId到上下文
                MDC.put("traceId", request.getHeader(AuthConstants.HEADER_TRACE_ID));
            }
            UserContextUtils.setHeaders(headers);
            filterChain.doFilter(request, response);
        } finally {
            UserContextUtils.clear();
        }
    }
}
