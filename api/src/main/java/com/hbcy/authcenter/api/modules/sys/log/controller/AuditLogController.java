package com.hbcy.authcenter.api.modules.sys.log.controller;

import com.hbcy.authcenter.api.modules.sys.log.model.AuditLog;
import com.hbcy.authcenter.api.modules.sys.log.service.IAuditLogService;
import com.hbcy.authcenter.api.modules.sys.log.vo.AuditLogQueryVO;
import com.hbcy.common.base.pojo.PageResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计日志
 * @author 姚泰然
 * @module sys
 * @date 2026-01-13 15:12
 */
@RestController
@RequestMapping("/api/portal/v1/sys/log")
public class AuditLogController {
    @Resource
    private IAuditLogService auditLogService;

    /**
     *  查询审计日志
     * @param vo 查询条件
     * @return 分页结果
     */
    @RequestMapping
    public PageResp<AuditLog> query(AuditLogQueryVO vo) {
        return auditLogService.query(vo);
    }
}
