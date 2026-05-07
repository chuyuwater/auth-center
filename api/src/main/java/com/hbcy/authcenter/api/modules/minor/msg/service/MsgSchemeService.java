package com.hbcy.authcenter.api.modules.minor.msg.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgChannel;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgChannelMapper;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSeqGenerator;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgLogMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgLog;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgSchemeChannelMapper;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgSchemeMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgScheme;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgSchemeChannel;
import com.hbcy.authcenter.api.common.bean.NameCacheService;
import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgSchemeDTO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.SchemeCreateVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.SchemeQueryVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.SchemeUpdateVO;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgTemplateMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgTemplate;
import com.hbcy.common.base.error.ClientError;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.base.util.BeanCopyUtils;
import com.hbcy.common.db.model.PageRespEx;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MsgSchemeService extends ServiceImpl<MsgSchemeMapper, MsgScheme> {
    @Resource
    private MsgSeqGenerator msgSeqGenerator;
    @Resource
    private MsgSchemeChannelMapper schemeChannelMapper;
    @Resource
    private MsgTemplateMapper templateMapper;
    @Resource
    private MsgChannelMapper channelMapper;
    @Resource
    private MsgLogMapper msgLogMapper;
    @Resource
    private NameCacheService nameCacheService;

    @Transactional(rollbackFor = Exception.class)
    public MsgSchemeDTO createScheme(SchemeCreateVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        String userId = UserContextUtils.getUserId();

        // 校验模板
        MsgTemplate template = templateMapper.selectById(vo.getTemplateId());
        if (template == null) {
            throw new ClientError("关联模板不存在");
        }

        MsgScheme scheme = new MsgScheme();
        BeanCopyUtils.copy(vo, scheme);
        scheme.setId(UlidCreator.getUlid().toString());
        scheme.setSchemeNo(msgSeqGenerator.generate(tenantId, MsgSeqGenerator.TYPE_SCHEME));
        scheme.setBizType(template.getMsgType());
        scheme.setRetryEnabled(vo.retryEnabled ? 1 : 0);
        scheme.setForbidden(0);
        scheme.setTenantId(tenantId);
        scheme.setCreateUser(userId);
        scheme.setUpdateUser(userId);
        baseMapper.insert(scheme);

        // 保存方案渠道关联
        saveSchemeChannels(scheme.getId(), vo.getChannelIds(), tenantId);
        return toDto(scheme);
    }

    @Transactional(rollbackFor = Exception.class)
    public MsgSchemeDTO updateScheme(String id, SchemeUpdateVO vo) {
        MsgScheme scheme = getById(id);
        if (scheme == null) {
            throw new ClientError("方案不存在");
        }
        if (vo.getSchemeName() != null) scheme.setSchemeName(vo.getSchemeName());
        if (vo.getGroupId() != null) scheme.setGroupId(vo.getGroupId());
        if (vo.getReceiverType() != null) scheme.setReceiverType(vo.getReceiverType());
        if (vo.getRuleId() != null) scheme.setRuleId(vo.getRuleId());
        if (vo.getRetryEnabled() != null) scheme.setRetryEnabled(vo.getRetryEnabled() ? 1 : 0);
        if (vo.getRetryInterval() != null) scheme.setRetryInterval(vo.getRetryInterval());
        if (vo.getRetryMaxCount() != null) scheme.setRetryMaxCount(vo.getRetryMaxCount());
        if (vo.getDescription() != null) scheme.setDescription(vo.getDescription());
        scheme.setUpdateUser(UserContextUtils.getUserId());
        baseMapper.updateById(scheme);

        // 更新渠道关联
        if (!CollectionUtils.isEmpty(vo.getChannelIds())) {
            schemeChannelMapper.delete(new QueryWrapper<MsgSchemeChannel>()
                    .eq(MsgSchemeChannel.COL_SCHEME_ID, id));
            saveSchemeChannels(id, vo.getChannelIds(), scheme.getTenantId());
        }
        return toDto(scheme);
    }

    public void toggleScheme(String id) {
        MsgScheme scheme = getById(id);
        if (scheme == null) {
            throw new ClientError("方案不存在");
        }
        scheme.setForbidden(scheme.getForbidden() == 0 ? 1 : 0);
        scheme.setUpdateUser(UserContextUtils.getUserId());
        baseMapper.updateById(scheme);
    }

    public void deleteScheme(String id) {
        MsgScheme scheme = getById(id);
        if (scheme == null) return;
        // 校验是否有关联日志
        Long logCount = msgLogMapper.selectCount(new QueryWrapper<MsgLog>()
                .eq(MsgLog.COL_SCHEME_ID, id));
        if (logCount > 0) {
            throw new ClientError("该方案已有关联消息日志，无法删除");
        }
        baseMapper.update(new UpdateWrapper<MsgScheme>()
                .eq(MsgScheme.COL_ID, id)
                .set(MsgScheme.COL_DELETE_TIME, System.currentTimeMillis())
                .set(MsgScheme.COL_UPDATE_USER, UserContextUtils.getUserId()));
        schemeChannelMapper.delete(new QueryWrapper<MsgSchemeChannel>()
                .eq(MsgSchemeChannel.COL_SCHEME_ID, id));
    }

    public PageResp<MsgSchemeDTO> queryScheme(SchemeQueryVO vo) {
        Page<MsgScheme> dbPage = vo.getDbPage();
        String tenantId = UserContextUtils.getTenantId();
        QueryWrapper<MsgScheme> qw = new QueryWrapper<MsgScheme>()
                .eq(MsgScheme.COL_TENANT_ID, tenantId)
                .eq(MsgScheme.COL_DELETE_TIME, 0)
                .eq(StringUtils.isNotBlank(vo.getGroupId()), MsgScheme.COL_GROUP_ID, vo.getGroupId())
                .in(!CollectionUtils.isEmpty(vo.getBizTypes()), MsgScheme.COL_BIZ_TYPE, vo.getBizTypes())
                .and(StringUtils.isNotBlank(vo.getKeyword()),
                        w -> w.like(MsgScheme.COL_SCHEME_NAME, vo.getKeyword())
                                .or().like(MsgScheme.COL_SCHEME_NO, vo.getKeyword()))
                .orderByDesc(MsgScheme.COL_CREATE_TIME);
        Page<MsgScheme> page = baseMapper.selectPage(dbPage, qw);
        Set<String> templateIds = page.getRecords().stream().map(MsgScheme::getTemplateId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<String, String> templateNameMap = templateMapper.selectBatchIds(templateIds).stream()
                .collect(Collectors.toMap(MsgTemplate::getId, MsgTemplate::getTemplateName));
        Set<String> userIds = page.getRecords().stream()
                .flatMap(s -> java.util.stream.Stream.of(s.getCreateUser(), s.getUpdateUser()))
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<String, String> userNameMap = nameCacheService.getUserNameMap(userIds);
        Page<MsgSchemeDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(s -> {
            MsgSchemeDTO dto = BeanCopyUtils.copy(s, MsgSchemeDTO.class);
            dto.setTemplateName(templateNameMap.getOrDefault(s.getTemplateId(), s.getTemplateId()));
            dto.setCreateUserName(userNameMap.getOrDefault(s.getCreateUser(), s.getCreateUser()));
            dto.setUpdateUserName(userNameMap.getOrDefault(s.getUpdateUser(), s.getUpdateUser()));
            return dto;
        }).collect(Collectors.toList()));
        return new PageRespEx<>(dtoPage);
    }

    public MsgSchemeDTO getSchemeDetail(String id) {
        MsgScheme scheme = getById(id);
        if (scheme == null) {
            throw new ClientError("方案不存在");
        }
        return toDto(scheme);
    }

    public List<MsgSchemeChannel> listSchemeChannels(String schemeId) {
        return schemeChannelMapper.selectList(new QueryWrapper<MsgSchemeChannel>()
                .eq(MsgSchemeChannel.COL_SCHEME_ID, schemeId));
    }

    /**
     * 根据方案编码查询方案
     */
    public MsgScheme getBySchemeNo(String schemeNo) {
        return getOne(new QueryWrapper<MsgScheme>()
                .eq(MsgScheme.COL_SCHEME_NO, schemeNo)
                .eq(MsgScheme.COL_DELETE_TIME, 0));
    }

    private void saveSchemeChannels(String schemeId, List<String> channelIds, String tenantId) {
        for (String channelId : channelIds) {
            MsgSchemeChannel sc = new MsgSchemeChannel();
            sc.setId(UlidCreator.getUlid().toString());
            sc.setSchemeId(schemeId);
            sc.setChannelId(channelId);
            sc.setTenantId(tenantId);
            schemeChannelMapper.insert(sc);
        }
    }

    private MsgSchemeDTO toDto(MsgScheme scheme) {
        MsgSchemeDTO dto = BeanCopyUtils.copy(scheme, MsgSchemeDTO.class);
        MsgTemplate template = templateMapper.selectById(scheme.getTemplateId());
        dto.setTemplateName(template != null ? template.getTemplateName() : scheme.getTemplateId());
        dto.setCreateUserName(nameCacheService.getUserName(scheme.getCreateUser()));
        dto.setUpdateUserName(nameCacheService.getUserName(scheme.getUpdateUser()));
        return dto;
    }
}
