package com.hbcy.authcenter.gateway.dto;

import lombok.Data;

import java.time.LocalDateTime;

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
     * 组织id
     */
    private String orgId;
    /**
     * 请求应用
     */
    private String srcApp;
    /**
     * 目标应用
     */
    private String targetApp;
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
     * query param
     */
    private String reqParm;
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
    private LocalDateTime reqTime;
    /**
     * 响应时间（毫秒）
     */
    private Long duration;
    /**
     * 客户端IP
     */
    private String clientIp;
}
