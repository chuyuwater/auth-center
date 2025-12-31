package com.hbcy.authcenter.api.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author 姚泰然
 * @date 2025-12-24 17:49
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class G {
    /**
     * 数据库里面树结构id全路径的分隔符
     */
    public static final String ID_PATH_SPLITTER = "/";

    public static final String PHONE_PATTERN = "^(?:\\+?86)?1[3-9]\\d{9}$";
    public static final String SESSION_TENANT_ID = "tenantId";
    private static final String NAME_CACHE_PREFIX = "portal:name:";
    public static final String ORG_NAME_CACHE_KEY = NAME_CACHE_PREFIX + "org:";
    public static final String USER_NAME_CACHE_KEY = NAME_CACHE_PREFIX + "user:";
}
