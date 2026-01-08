package com.hbcy.authcenter.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author 姚泰然
 * @date 2025-12-23 12:10
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthConstants {
    /**
     * SDK注入的Header
     */
    public static final String HEADER_SDK_VERSION = "X-AUTH-SDK-VERSION";
    public static final String SDK_VERSION = "1.0";

    /***************************以下为网关注入header************************************/
    //当前用户id
    public static final String HEADER_USER_ID = "X-USER-ID";
    //当前租户id
    public static final String HEADER_TENANT_ID = "X-TENANT-ID";
    //当前用户是租户默认管理员，用于特殊权限校验
    public static final String HEADER_ADMIN_FLAG = "X-ADMIN-FLAG";
    //手动trace标记（如不使用OpenTelemetry）
    public static final String HEADER_TRACE_ID = "X-TRACE-ID";

    /****************************以下为前端传入header*************************************/
    //当前用户token
    public static final String HEADER_TOKEN = "X-AUTH-TOKEN";
    //前端用户切换到的组织id
    public static final String HEADER_ORG_ID = "X-ORG-ID";
    //当前应用id
    public static final String HEADER_APP_ID = "X-APP-ID";
}
