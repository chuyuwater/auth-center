package com.hbcy.authcenter.sdk.utils;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.hbcy.authcenter.sdk.constants.AuthConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author 姚泰然
 * @date 2025-12-23 12:23
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserContextUtils {
    // 使用 TTL 替代原生 ThreadLocal
    private static final ThreadLocal<Map<String, String>> HEADER = new TransmittableThreadLocal<>();

    public static Map<String, String> getHeaders() {
        return HEADER.get();
    }

    public static void setHeaders(Map<String, String> headers) {
        HEADER.set(headers);
    }

    public static String getHeader(String key) {
        return getHeaders().get(key);
    }

    public static String getUserId() {
        return getHeader(AuthConstants.HEADER_USER_ID);
    }

    public static String getUserOrg() {
        return getHeader(AuthConstants.HEADER_ORG_ID);
    }

    public static String getAppId() {
        return getHeader(AuthConstants.HEADER_APP_ID);
    }

    public static String getTenantId() {
        return getHeader(AuthConstants.HEADER_TENANT_ID);
    }

    public static String getTraceId() {
        return getHeader(AuthConstants.HEADER_TRACE_ID);
    }

    public static String getSdkVersion() {
        return getHeader(AuthConstants.HEADER_SDK_VERSION);
    }

    /**
     * 请求是否来自SDK
     */
    public static boolean isSdkReq() {
        return StringUtils.isBlank(getSdkVersion());
    }

    public static void clear() {
        HEADER.remove();
    }
}
