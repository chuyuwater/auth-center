package com.hbcy.authcenter.api.modules.sys.log.service.impl;

import com.hbcy.authcenter.api.modules.sys.log.model.AuditLog;
import com.hbcy.authcenter.api.modules.sys.log.service.IAuditLogService;
import com.hbcy.authcenter.api.modules.sys.log.vo.AuditLogQueryVO;
import com.hbcy.common.base.pojo.PageResp;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 时序数据库日志服务
 * @author 姚泰然
 * @date 2026-01-13 14:42
 */
@Component
@ConditionalOnProperty(name = "app.log.type", havingValue = "tsdb")
public class TsdbLogServiceImpl implements IAuditLogService {
    @Override
    public void save(List<AuditLog> dto) {

    }

    @Override
    public PageResp<AuditLog> query(AuditLogQueryVO vo) {
        return null;
    }
}
