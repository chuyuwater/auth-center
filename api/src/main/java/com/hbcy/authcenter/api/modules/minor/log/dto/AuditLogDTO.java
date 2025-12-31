package com.hbcy.authcenter.api.modules.minor.log.dto;

import lombok.Data;

import java.util.Map;

/**
 * 审计日志内容
 *
 * @author 姚泰然
 * @date 2025-12-17 14:41
 */
@Data
public class AuditLogDTO {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 请求头
     */
    private Map<String, String> reqHeader;
    /**
     * 请求应用
     */
    private String appId;
    /**
     * 请求方法
     */
    private String reqMethod;

    /**
     * host
     */
    private String reqHost;
    /**
     * path
     */
    private String reqPath;
    /**
     * param
     */
    private Map<String, String> reqParam;
    /**
     * body
     */
    private String reqBody;
    /**
     * resp code
     */
    private int respCode;
    /**
     * resp body
     */
    private String respBody;
    /**
     * 请求时间戳
     */
    private Long reqTime;
    /**
     * 响应时间戳
     */
    private Long respTime;
    /**
     * 客户端IP
     */
    private String clientIp;
}
