package com.hbcy.authcenter.global.constants;

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
    //OpenTelemetry注入的header
    public static final String HEADER_TRACE_PARENT = "traceparent";

    /***************************以下为网关注入header************************************/
    //当前用户id
    public static final String HEADER_USER_ID = "X-USER-ID";
    //当前用户名称
    public static final String HEADER_USER_NAME = "X-USER-NAME";
    //当前租户id
    public static final String HEADER_TENANT_ID = "X-TENANT-ID";
    //当前用户是租户默认管理员，用于特殊权限校验
    public static final String HEADER_ADMIN_FLAG = "X-ADMIN-FLAG";
    //用户请求ip，某些命令需要
    public static final String HEADER_USER_IP = "X-USER-IP";
    /****************************以下为前端传入header*************************************/
    //当前用户token
    public static final String HEADER_TOKEN = "X-AUTH-TOKEN";
    //前端用户切换到的组织id
    public static final String HEADER_ORG_ID = "X-ORG-ID";
    //当前应用id
    public static final String HEADER_APP_ID = "X-APP-ID";
    //从链路追踪中提取的traceId
    public static final String HEADER_TRACE_ID = "X-TRACE-ID";
    /********************************后端AK/SK访问**************************************/
    //AK，当不使用ip白名单时，需要结合下面的key来校验
    public static final String HEADER_AK = "X-AUTH-AK";
    //摘要签名
    public static final String HEADER_SIGN = "X-AUTH-SIGN";
    //时间戳
    public static final String HEADER_TIMESTAMP = "X-AUTH-TIMESTAMP";
    //随机数
    public static final String HEADER_NONCE = "X-AUTH-NONCE";
}
