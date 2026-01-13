package com.hbcy.authcenter.api.modules.minor.log.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2026-01-13 14:02
 */
@Data
public class AuditLogQueryVO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String targetApp;
    private Integer reqMethod;
    private String reqPath;
    private String clientIp;
    private String userId;
}
