package com.hbcy.authcenter.api.modules.sys.log.vo;

import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 查询审计日志条件
 * @author 姚泰然
 * @date 2026-01-13 14:02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLogQueryVO extends PageVO {
    /**
     * 请求时间（起始）
     */
    private LocalDateTime startTime;
    /**
     * 请求时间（结束）
     */
    private LocalDateTime endTime;
    /**
     * 用户访问应用id
     */
    private String targetApp;
    /**
     * 请求方法：GET/POST/PUT/DELETE
     */
    private String reqMethod;
    /**
     * 请求API路径，前缀匹配
     */
    private String reqPath;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 组织ID
     */
    private String orgId;
}
