package com.hbcy.authcenter.sdk.constants;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 姚泰然
 * @date 2025-12-23 12:10
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthConstants {
    public static final String HEADER_SDK_VERSION = "X-SDK-VERSION";
    public static final String SDK_VERSION = "1.0";

    public static final String HEADER_USER_ID = "X-USER-ID";
    public static final String HEADER_APP_ID = "X-APP-ID";
    public static final String HEADER_TENANT_ID = "X-TENANT-ID";
    //手动trace标记（如不使用OpenTelemetry）
    public static final String HEADER_TRACE_ID = "X-TRACE-ID";
    public static final String HEADER_SUB_DOMAIN = "X-SUB-DOMAIN";
    public static final String HEADER_OBJ_DOMAIN = "X-OBJ-DOMAIN";

    public static final String DOMAIN_ORG_ID = "org";

    public static Map<String, String> parseSubDomain(HttpServletRequest request) {
        return parseDomain(request, HEADER_SUB_DOMAIN);
    }

    public static Map<String, String> parseObjDomain(HttpServletRequest request) {
        return parseDomain(request, HEADER_OBJ_DOMAIN);
    }

    private static Map<String, String> parseDomain(HttpServletRequest request, String header) {
        Map<String, String> map = new HashMap<>();
        String value = request.getHeader(header);
        if (StringUtils.isNotBlank(header)) {
            //split by ; then split by =
            // like: prj=123;org=356
            String[] domains = value.split(";");
            for (String domain : domains) {
                String[] kv = domain.split("=");
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}
