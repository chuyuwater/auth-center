package com.hbcy.authcenter.gateway.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author 姚泰然
 * @date 2026-01-08 08:24
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GatewayConstants {
    /**
     * session中租户id的key
     */
    public static final String SESSION_TENANT_ID = "tenantId";
    /**
     * session中用户姓名key
     */
    public static final String SESSION_USER_NAME = "userName";
    /**
     * 用户权限缓存（按userId:orgId）
     */
    public static final String USER_PERM_CACHE_PREFIX = "portal:auth:user:perm:%s:%s";
    /**
     * 发送审计日志的kafka topic
     */
    public static final String KAFKA_TOPIC_AUDIT_LOG = "audit_log";

    /**
     * 访问ak缓存
     */
    public static final String USER_ACCESS_KEY_PREFIX = "portal:auth:ak:";

}
