package com.hbcy.authcenter.api.modules.sys.log.service;

import com.hbcy.authcenter.api.modules.sys.log.model.AuditLog;
import com.hbcy.authcenter.api.modules.sys.log.vo.AuditLogQueryVO;
import com.hbcy.common.base.pojo.PageResp;

import java.util.List;

/**
 * 审计日志服务抽象
 * 暂时先存在关系型数据库，后续迁移到时序数据库避免数据膨胀
 *
 * @author 姚泰然
 * @date 2026-01-13 13:59
 */
public interface IAuditLogService {
    /**
     * 保存审计日志
     *
     * @param dto 审计日志
     */
    void save(List<AuditLog> dto);

    /**
     * 查询审计日志
     * @param vo 查询条件
     * @return 分页结果
     */
    PageResp<AuditLog> query(AuditLogQueryVO vo);
}
