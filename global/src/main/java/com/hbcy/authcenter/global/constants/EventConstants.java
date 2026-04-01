package com.hbcy.authcenter.global.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author 姚泰然
 * @date 2025-12-31 14:13
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventConstants {
    /**
     * 门户相关应用事件的topic
     */
    public static final String KAFKA_TOPIC_PORTAL_EVENT = "app-event-portal";
    /**
     * 新增租户，info是租户的详情
     */
    public static final String TENANT_CREATED = "TENANT_CREATED";
    /**
     * 租户管理员变更，info是租户的详情
     */
    public static final String TENANT_ADMIN_CHANGED = "TENANT_ADMIN_CHANGED";
    /**
     * app的权限资源发生变更，网关需要重建缓存
     */
    public static final String RESOURCE_PERM_CHANGED = "RESOURCE_PERM_CHANGED";
    /**
     * 用户查看待办
     */
    public static final String USER_READ_TODO = "USER_READ_TODO";
    /**
     * 用户登录
     */
    public static final String USER_LOGIN = "USER_LOGIN";
}
