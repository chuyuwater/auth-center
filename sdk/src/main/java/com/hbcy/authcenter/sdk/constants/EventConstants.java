package com.hbcy.authcenter.sdk.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author 姚泰然
 * @date 2025-12-31 14:13
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventConstants {
    /**
     * kafka topic
     */
    public static final String KAFKA_TOPIC_APP_EVENT = "app-event";
    /**
     * 新增租户，info是租户的详情
     */
    public static final String TENANT_CREATED = "TENANT_CREATED";
    /**
     * 租户管理员变更，info是租户的详情
     */
    public static final String TENANT_ADMIN_CHANGED = "TENANT_ADMIN_CHANGED";
}
