package com.hbcy.authcenter.api.modules.sys.log.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbcy.authcenter.api.modules.sys.log.dao.AuditLogMapper;
import com.hbcy.authcenter.api.modules.sys.log.model.AuditLog;
import com.hbcy.authcenter.api.modules.sys.log.service.IAuditLogService;
import com.hbcy.authcenter.api.modules.sys.log.vo.AuditLogQueryVO;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.db.model.PageRespEx;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 关系型数据库日志服务
 * @author 姚泰然
 * @date 2026-01-13 14:41
 */
@Component
@ConditionalOnProperty(name = "app.log.type", havingValue = "rds", matchIfMissing = true)
public class RdsLogServiceImpl implements IAuditLogService {
    @Resource
    private AuditLogMapper auditLogMapper;

    @Override
    public void save(List<AuditLog> dto) {
        auditLogMapper.insertIgnore(dto);
    }

    @Override
    public PageResp<AuditLog> query(AuditLogQueryVO vo) {
        Page<Object> dbPage = vo.getDbPage();
        Page<AuditLog> page = auditLogMapper.listLog(dbPage, vo);
        return new PageRespEx<>(page);
    }
}
