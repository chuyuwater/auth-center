package com.hbcy.authcenter.api.modules.sys.log.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbcy.authcenter.api.modules.sys.log.model.AuditLog;
import com.hbcy.authcenter.api.modules.sys.log.vo.AuditLogQueryVO;
import org.apache.ibatis.annotations.Param;

/**
 * @author 姚泰然
 * @date 2026-01-13 15:36
 */
public interface AuditLogMapper extends BaseMapper<AuditLog> {
    Page<AuditLog> listLog(Page<?> dbPage, @Param("vo") AuditLogQueryVO vo);
}