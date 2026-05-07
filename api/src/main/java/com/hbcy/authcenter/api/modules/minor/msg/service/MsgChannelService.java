package com.hbcy.authcenter.api.modules.minor.msg.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgChannelMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgChannel;
import com.hbcy.authcenter.api.modules.minor.msg.vo.ChannelCreateVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.ChannelQueryVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.ChannelUpdateVO;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSeqGenerator;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgSchemeChannelMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgSchemeChannel;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgTemplateContentMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgTemplateContent;
import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgChannelDTO;
import com.hbcy.common.base.error.ClientError;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.base.util.BeanCopyUtils;
import com.hbcy.common.db.model.PageRespEx;
import com.hbcy.authcenter.api.common.bean.NameCacheService;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息渠道 Service
 */
@Service
public class MsgChannelService extends ServiceImpl<MsgChannelMapper, MsgChannel> {
    @Resource
    private MsgSeqGenerator msgSeqGenerator;
    @Resource
    private MsgTemplateContentMapper templateContentMapper;
    @Resource
    private MsgSchemeChannelMapper schemeChannelMapper;
    @Resource
    private NameCacheService nameCacheService;

    public MsgChannelDTO createChannel(ChannelCreateVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        String userId = UserContextUtils.getUserId();

        MsgChannel channel = BeanCopyUtils.copy(vo, MsgChannel.class);
        channel.setId(UlidCreator.getUlid().toString());
        channel.setChannelNo(msgSeqGenerator.generate(tenantId, MsgSeqGenerator.TYPE_CHANNEL));
        channel.setForbidden(0);
        channel.setTenantId(tenantId);
        channel.setCreateUser(userId);
        channel.setUpdateUser(userId);
        baseMapper.insert(channel);
        return toDto(channel);
    }

    public MsgChannelDTO updateChannel(String id, ChannelUpdateVO vo) {
        MsgChannel channel = getById(id);
        if (channel == null) {
            throw new ClientError("渠道不存在");
        }
        if (vo.getChannelName() != null) {
            channel.setChannelName(vo.getChannelName());
        }
        if (vo.getConfigJson() != null) {
            channel.setConfigJson(vo.getConfigJson());
        }
        if (vo.getDescription() != null) {
            channel.setDescription(vo.getDescription());
        }
        channel.setUpdateUser(UserContextUtils.getUserId());
        baseMapper.updateById(channel);
        return toDto(channel);
    }

    public void toggleChannel(String id) {
        MsgChannel channel = getById(id);
        if (channel == null) {
            throw new ClientError("渠道不存在");
        }
        // 禁用前校验
        if (channel.getForbidden() == 0) {
            Long refCount = templateContentMapper.selectCount(new QueryWrapper<MsgTemplateContent>()
                    .eq(MsgTemplateContent.COL_CHANNEL_ID, id));
            if (refCount > 0) {
                throw new ClientError("该渠道已被模板内容引用，无法禁用");
            }
        }
        channel.setForbidden(channel.getForbidden() == 0 ? 1 : 0);
        channel.setUpdateUser(UserContextUtils.getUserId());
        baseMapper.updateById(channel);
    }

    public void deleteChannel(String id) {
        MsgChannel channel = getById(id);
        if (channel == null) {
            return;
        }
        // 删除前校验：是否被方案引用
        Long refCount = schemeChannelMapper.selectCount(new QueryWrapper<MsgSchemeChannel>()
                .eq(MsgSchemeChannel.COL_CHANNEL_ID, id));
        if (refCount > 0) {
            throw new ClientError("该渠道已被发送方案引用，无法删除");
        }
        baseMapper.update(new UpdateWrapper<MsgChannel>()
                .eq(MsgChannel.COL_ID, id)
                .set(MsgChannel.COL_DELETE_TIME, System.currentTimeMillis())
                .set(MsgChannel.COL_UPDATE_USER, UserContextUtils.getUserId()));
    }

    public PageResp<MsgChannelDTO> queryChannel(ChannelQueryVO vo) {
        Page<MsgChannel> dbPage = vo.getDbPage();
        String tenantId = UserContextUtils.getTenantId();
        QueryWrapper<MsgChannel> qw = new QueryWrapper<MsgChannel>()
                .eq(MsgChannel.COL_TENANT_ID, tenantId)
                .eq(MsgChannel.COL_DELETE_TIME, 0)
                .eq(StringUtils.isNotBlank(vo.getChannelType()), MsgChannel.COL_CHANNEL_TYPE, vo.getChannelType())
                .and(StringUtils.isNotBlank(vo.getKeyword()),
                        w -> w.like(MsgChannel.COL_CHANNEL_NAME, vo.getKeyword())
                                .or().like(MsgChannel.COL_CHANNEL_NO, vo.getKeyword()))
                .orderByDesc(MsgChannel.COL_CREATE_TIME);
        Page<MsgChannel> page = baseMapper.selectPage(dbPage, qw);
        Set<String> userIds = page.getRecords().stream()
                .flatMap(c -> java.util.stream.Stream.of(c.getCreateUser(), c.getUpdateUser()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, String> userNameMap = nameCacheService.getUserNameMap(userIds);
        Page<MsgChannelDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(c -> {
            MsgChannelDTO dto = BeanCopyUtils.copy(c, MsgChannelDTO.class);
            dto.setCreateUserName(userNameMap.getOrDefault(c.getCreateUser(), c.getCreateUser()));
            dto.setUpdateUserName(userNameMap.getOrDefault(c.getUpdateUser(), c.getUpdateUser()));
            return dto;
        }).collect(Collectors.toList()));
        return new PageRespEx<>(dtoPage);
    }

    public MsgChannelDTO getChannelDetail(String id) {
        MsgChannel channel = getById(id);
        if (channel == null) {
            throw new ClientError("渠道不存在");
        }
        return toDto(channel);
    }

    private MsgChannelDTO toDto(MsgChannel channel) {
        MsgChannelDTO dto = BeanCopyUtils.copy(channel, MsgChannelDTO.class);
        dto.setCreateUserName(nameCacheService.getUserName(channel.getCreateUser()));
        dto.setUpdateUserName(nameCacheService.getUserName(channel.getUpdateUser()));
        return dto;
    }
}
