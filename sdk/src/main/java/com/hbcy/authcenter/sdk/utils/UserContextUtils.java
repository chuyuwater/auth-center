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

    /**
     * 获取所有特殊header
     *
     * @return header map
     */
    public static Map<String, String> getHeaders() {
        return HEADER.get();
    }

    /**
     * 修改header，sdk使用者不要调用该接口
     */
    public static void setHeaders(Map<String, String> headers) {
        HEADER.set(headers);
    }

    private static String getHeader(String key) {
        return getHeaders().get(key);
    }

    /**
     * 获取当前请求的用户
     *
     * @return 用户id
     */
    public static String getUserId() {
        return getHeader(AuthConstants.HEADER_USER_ID);
    }

    /**
     * 获取当前请求用户切换的组织
     *
     * @return 组织id
     */
    public static String getUserOrg() {
        return getHeader(AuthConstants.HEADER_ORG_ID);
    }

    /**
     * 获取当前用户在访问的应用
     *
     * @return 应用id
     */
    public static String getAppId() {
        return getHeader(AuthConstants.HEADER_APP_ID);
    }

    /**
     * 获取当前应用对应的租户
     *
     * @return 租户id
     */
    public static String getTenantId() {
        return getHeader(AuthConstants.HEADER_TENANT_ID);
    }

    /**
     * 获取网关生成的traceId
     *
     * @return traceId
     */
    public static String getTraceId() {
        return getHeader(AuthConstants.HEADER_TRACE_ID);
    }

    /**
     * 获取请求客户端的sdk版本，非sdk访问为null
     *
     * @return sdk版本
     */
    public static String getSdkVersion() {
        return getHeader(AuthConstants.HEADER_SDK_VERSION);
    }

    /**
     * 当前用户是否租户的默认管理员
     * 租户的默认管理员无论在哪个组织，都拥有租户在应用中的最大权限
     *
     * @return 是或否
     */
    public static boolean isTenantAdmin() {
        return "1".equals(getHeader(AuthConstants.HEADER_ADMIN_FLAG));
    }

    /**
     * 请求是否来自SDK
     */
    public static boolean isSdkReq() {
        return StringUtils.isBlank(getSdkVersion());
    }

    /**
     * 清理上下文
     * sdk使用者不要调用
     */
    public static void clear() {
        HEADER.remove();
    }
}
