package com.hbcy.authcenter.api.modules.minor.msg.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.common.bean.NameCacheService;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgLogMapper;
import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgLogDTO;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgLog;
import com.hbcy.authcenter.api.modules.minor.msg.vo.MsgLogQueryVO;
import com.hbcy.common.base.error.ClientError;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.base.util.BeanCopyUtils;
import com.hbcy.common.db.model.PageRespEx;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MsgLogService extends ServiceImpl<MsgLogMapper, MsgLog> {
    @Resource
    private NameCacheService nameCacheService;

    public PageResp<MsgLogDTO> queryMsgLog(MsgLogQueryVO vo) {
        Page<MsgLog> dbPage = vo.getDbPage();
        String tenantId = UserContextUtils.getTenantId();
        QueryWrapper<MsgLog> qw = new QueryWrapper<MsgLog>()
                .eq(MsgLog.COL_TENANT_ID, tenantId)
                .like(StringUtils.isNotBlank(vo.getKeyword()), MsgLog.COL_MSG_TITLE, vo.getKeyword())
                .eq(StringUtils.isNotBlank(vo.getBizEntity()), MsgLog.COL_BIZ_ENTITY, vo.getBizEntity())
                .eq(StringUtils.isNotBlank(vo.getMsgType()), MsgLog.COL_MSG_TYPE, vo.getMsgType())
                .eq(vo.getSendStatus() != null, MsgLog.COL_SEND_STATUS, vo.getSendStatus())
                .ge(vo.getReceiveTimeStart() != null, MsgLog.COL_RECEIVE_TIME, vo.getReceiveTimeStart())
                .le(vo.getReceiveTimeEnd() != null, MsgLog.COL_RECEIVE_TIME, vo.getReceiveTimeEnd())
                .orderByDesc(MsgLog.COL_CREATE_TIME);
        Page<MsgLog> page = baseMapper.selectPage(dbPage, qw);
        Set<String> userIds = page.getRecords().stream()
                .map(MsgLog::getTargetUser)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, String> userNameMap = nameCacheService.getUserNameMap(userIds);
        Page<MsgLogDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(log -> {
            MsgLogDTO dto = BeanCopyUtils.copy(log, MsgLogDTO.class);
            dto.setTargetUserName(userNameMap.getOrDefault(log.getTargetUser(), log.getTargetUser()));
            return dto;
        }).collect(Collectors.toList()));
        return new PageRespEx<>(dtoPage);
    }

    public MsgLogDTO getMsgLogDetail(String id) {
        MsgLog log = getById(id);
        if (log == null) {
            throw new ClientError("消息日志不存在");
        }
        MsgLogDTO dto = BeanCopyUtils.copy(log, MsgLogDTO.class);
        dto.setTargetUserName(nameCacheService.getUserName(log.getTargetUser()));
        return dto;
    }

    public void markAsRead(String id) {
        baseMapper.update(new UpdateWrapper<MsgLog>()
                .eq(MsgLog.COL_ID, id)
                .set(MsgLog.COL_VIEW_STATUS, MsgLog.VIEW_STATUS_READ));
    }

    public void markAllAsReadByUser(String userId) {
        baseMapper.update(new UpdateWrapper<MsgLog>()
                .eq(MsgLog.COL_TARGET_USER, userId)
                .eq(MsgLog.COL_VIEW_STATUS, MsgLog.VIEW_STATUS_UNREAD)
                .eq(MsgLog.COL_SEND_STATUS, MsgLog.SEND_STATUS_SUCCESS)
                .set(MsgLog.COL_VIEW_STATUS, MsgLog.VIEW_STATUS_READ));
    }
}
