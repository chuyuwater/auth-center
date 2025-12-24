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
            MDC.put("appId", request.getHeader(AuthConstants.HEADER_APP_ID));
            MDC.put("userId", request.getHeader(AuthConstants.HEADER_USER_ID));
            if (request.getHeader(AuthConstants.HEADER_TRACE_ID) != null) {
                headers.put(AuthConstants.HEADER_TRACE_ID, request.getHeader(AuthConstants.HEADER_TRACE_ID));
                //注入traceId到上下文
                MDC.put("traceId", request.getHeader(AuthConstants.HEADER_TRACE_ID));
            }
            Map<String, String> domains = new HashMap<>();
            if (request.getHeader(AuthConstants.HEADER_OBJ_DOMAIN) != null) {
                headers.put(AuthConstants.HEADER_OBJ_DOMAIN, request.getHeader(AuthConstants.HEADER_OBJ_DOMAIN));
                domains.putAll(AuthConstants.parseObjDomain(request));
                MDC.put("objDomain", request.getHeader(AuthConstants.HEADER_OBJ_DOMAIN));
            }
            if (request.getHeader(AuthConstants.HEADER_SUB_DOMAIN) != null) {
                headers.put(AuthConstants.HEADER_SUB_DOMAIN, request.getHeader(AuthConstants.HEADER_SUB_DOMAIN));
                domains.putAll(AuthConstants.parseSubDomain(request));
                MDC.put("subDomain", request.getHeader(AuthConstants.HEADER_SUB_DOMAIN));
            }
            UserContextUtils.setHeaders(headers);
            UserContextUtils.setDomains(domains);
            filterChain.doFilter(request, response);
        } finally {
            UserContextUtils.clear();
        }
    }
}
