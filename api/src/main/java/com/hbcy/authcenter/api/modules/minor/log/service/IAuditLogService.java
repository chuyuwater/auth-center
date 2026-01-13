package com.hbcy.authcenter.api.modules.minor.log.service;

import com.hbcy.authcenter.gateway.dto.AuditLogDTO;

/**
 * 审计日志服务抽象
 * 暂时先存在关系型数据库，后续迁移到时序数据库避免数据膨胀
 *
 * @author 姚泰然
 * @date 2026-01-13 13:59
 */
public interface IAuditLogService {
    void save(AuditLogDTO dto);
}
