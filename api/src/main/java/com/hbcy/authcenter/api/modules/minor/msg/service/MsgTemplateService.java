package com.hbcy.authcenter.api.modules.minor.msg.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSeqGenerator;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgSchemeMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgScheme;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgTemplateContentMapper;
import com.hbcy.authcenter.api.modules.minor.msg.dao.MsgTemplateMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgTemplate;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgTemplateContent;
import com.hbcy.authcenter.api.common.bean.NameCacheService;
import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgTemplateContentDTO;
import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgTemplateDTO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.TemplateContentVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.TemplateCreateVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.TemplateQueryVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.TemplateUpdateVO;
import com.hbcy.common.base.error.ClientError;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.base.util.BeanCopyUtils;
import com.hbcy.common.db.model.PageRespEx;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息模板 Service
 */
@Service
public class MsgTemplateService extends ServiceImpl<MsgTemplateMapper, MsgTemplate> {
    @Resource
    private MsgSeqGenerator msgSeqGenerator;
    @Resource
    private MsgTemplateContentMapper contentMapper;
    @Resource
    private MsgSchemeMapper schemeMapper;
    @Resource
    private NameCacheService nameCacheService;

    @Transactional(rollbackFor = Exception.class)
    public MsgTemplateDTO createTemplate(TemplateCreateVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        String userId = UserContextUtils.getUserId();

        MsgTemplate template = BeanCopyUtils.copy(vo, MsgTemplate.class);
        template.setId(UlidCreator.getUlid().toString());
        template.setTemplateNo(msgSeqGenerator.generate(tenantId, MsgSeqGenerator.TYPE_TEMPLATE));
        template.setForbidden(0);
        template.setTenantId(tenantId);
        template.setCreateUser(userId);
        template.setUpdateUser(userId);
        baseMapper.insert(template);

        // 创建模板内容
        int order = 0;
        for (TemplateContentVO contentVO : vo.getContents()) {
            MsgTemplateContent content = buildContent(template.getId(), contentVO, tenantId, userId, order++);
            contentMapper.insert(content);
        }
        return toDto(template);
    }

    @Transactional(rollbackFor = Exception.class)
    public MsgTemplateDTO updateTemplate(String id, TemplateUpdateVO vo) {
        MsgTemplate template = getById(id);
        if (template == null) {
            throw new ClientError("模板不存在");
        }
        if (vo.getTemplateName() != null) template.setTemplateName(vo.getTemplateName());
        if (vo.getMsgType() != null) template.setMsgType(vo.getMsgType());
        if (vo.getBizEntity() != null) template.setBizEntity(vo.getBizEntity());
        if (vo.getGroupId() != null) template.setGroupId(vo.getGroupId());
        if (vo.getDescription() != null) template.setDescription(vo.getDescription());
        template.setUpdateUser(UserContextUtils.getUserId());
        baseMapper.updateById(template);
        return toDto(template);
    }

    public void toggleTemplate(String id) {
        MsgTemplate template = getById(id);
        if (template == null) {
            throw new ClientError("模板不存在");
        }
        if (template.getForbidden() == 0) {
            Long refCount = schemeMapper.selectCount(new QueryWrapper<MsgScheme>()
                    .eq(MsgScheme.COL_TEMPLATE_ID, id));
            if (refCount > 0) {
                throw new ClientError("该模板已被发送方案引用，无法禁用");
            }
        }
        template.setForbidden(template.getForbidden() == 0 ? 1 : 0);
        template.setUpdateUser(UserContextUtils.getUserId());
        baseMapper.updateById(template);
    }

    public void deleteTemplate(String id) {
        MsgTemplate template = getById(id);
        if (template == null) return;
        // 逻辑删除模板
        baseMapper.update(new UpdateWrapper<MsgTemplate>()
                .eq(MsgTemplate.COL_ID, id)
                .set(MsgTemplate.COL_DELETE_TIME, System.currentTimeMillis())
                .set(MsgTemplate.COL_UPDATE_USER, UserContextUtils.getUserId()));
        // 逻辑删除模板内容
        contentMapper.update(new UpdateWrapper<MsgTemplateContent>()
                .eq(MsgTemplateContent.COL_TEMPLATE_ID, id)
                .set(MsgTemplateContent.COL_DELETE_TIME, System.currentTimeMillis()));
    }

    public PageResp<MsgTemplateDTO> queryTemplate(TemplateQueryVO vo) {
        Page<MsgTemplate> dbPage = vo.getDbPage();
        String tenantId = UserContextUtils.getTenantId();
        QueryWrapper<MsgTemplate> qw = new QueryWrapper<MsgTemplate>()
                .eq(MsgTemplate.COL_TENANT_ID, tenantId)
                .eq(MsgTemplate.COL_DELETE_TIME, 0)
                .eq(StringUtils.isNotBlank(vo.getMsgType()), MsgTemplate.COL_MSG_TYPE, vo.getMsgType())
                .eq(StringUtils.isNotBlank(vo.getGroupId()), MsgTemplate.COL_GROUP_ID, vo.getGroupId())
                .and(StringUtils.isNotBlank(vo.getKeyword()),
                        w -> w.like(MsgTemplate.COL_TEMPLATE_NAME, vo.getKeyword())
                                .or().like(MsgTemplate.COL_TEMPLATE_NO, vo.getKeyword()))
                .orderByDesc(MsgTemplate.COL_CREATE_TIME);
        Page<MsgTemplate> page = baseMapper.selectPage(dbPage, qw);
        return new PageRespEx<>(toDtoPage(page));
    }

    public MsgTemplateDTO getTemplateDetail(String id) {
        MsgTemplate template = getById(id);
        if (template == null) {
            throw new ClientError("模板不存在");
        }
        return toDto(template);
    }

    public List<MsgTemplateContentDTO> listContents(String templateId) {
        List<MsgTemplateContent> list = contentMapper.selectList(new QueryWrapper<MsgTemplateContent>()
                .eq(MsgTemplateContent.COL_TEMPLATE_ID, templateId)
                .eq(MsgTemplateContent.COL_DELETE_TIME, 0)
                .orderByAsc(MsgTemplateContent.COL_SHOW_ORDER));
        return list.stream().map(this::toContentDto).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public MsgTemplateContentDTO addContent(String templateId, TemplateContentVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        String userId = UserContextUtils.getUserId();
        Integer maxOrder = getMaxShowOrder(templateId);
        MsgTemplateContent content = buildContent(templateId, vo, tenantId, userId, maxOrder + 1);
        contentMapper.insert(content);
        return toContentDto(content);
    }

    @Transactional(rollbackFor = Exception.class)
    public MsgTemplateContentDTO updateContent(String contentId, TemplateContentVO vo) {
        MsgTemplateContent content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new ClientError("模板内容不存在");
        }
        if (vo.getMsgTitle() != null) content.setMsgTitle(vo.getMsgTitle());
        if (vo.getMsgContent() != null) content.setMsgContent(vo.getMsgContent());
        if (vo.getChannelId() != null) content.setChannelId(vo.getChannelId());
        if (vo.getThirdTemplateId() != null) content.setThirdTemplateId(vo.getThirdTemplateId());
        if (vo.getShowOrder() != null) content.setShowOrder(vo.getShowOrder());
        content.setUpdateUser(UserContextUtils.getUserId());
        contentMapper.updateById(content);
        return toContentDto(content);
    }

    @Transactional(rollbackFor = Exception.class)
    public MsgTemplateContentDTO copyContent(String contentId) {
        MsgTemplateContent source = contentMapper.selectById(contentId);
        if (source == null) {
            throw new ClientError("模板内容不存在");
        }
        String userId = UserContextUtils.getUserId();
        Integer maxOrder = getMaxShowOrder(source.getTemplateId());
        MsgTemplateContent copy = new MsgTemplateContent();
        copy.setId(UlidCreator.getUlid().toString());
        copy.setTemplateId(source.getTemplateId());
        copy.setMsgTitle(source.getMsgTitle());
        copy.setMsgContent(source.getMsgContent());
        copy.setChannelId(source.getChannelId());
        copy.setThirdTemplateId(source.getThirdTemplateId());
        copy.setShowOrder(maxOrder + 1);
        copy.setTenantId(source.getTenantId());
        copy.setCreateUser(userId);
        copy.setUpdateUser(userId);
        contentMapper.insert(copy);
        return toContentDto(copy);
    }

    public void deleteContent(String contentId) {
        contentMapper.update(new UpdateWrapper<MsgTemplateContent>()
                .eq(MsgTemplateContent.COL_ID, contentId)
                .set(MsgTemplateContent.COL_DELETE_TIME, System.currentTimeMillis()));
    }

    private MsgTemplateContent buildContent(String templateId, TemplateContentVO vo, String tenantId, String userId, int order) {
        MsgTemplateContent content = new MsgTemplateContent();
        content.setId(UlidCreator.getUlid().toString());
        content.setTemplateId(templateId);
        content.setMsgTitle(vo.getMsgTitle());
        content.setMsgContent(vo.getMsgContent());
        content.setChannelId(vo.getChannelId());
        content.setThirdTemplateId(vo.getThirdTemplateId() != null ? vo.getThirdTemplateId() : "");
        content.setShowOrder(vo.getShowOrder() != null ? vo.getShowOrder() : order);
        content.setTenantId(tenantId);
        content.setCreateUser(userId);
        content.setUpdateUser(userId);
        content.setDeleteTime(0L);
        return content;
    }

    private Integer getMaxShowOrder(String templateId) {
        List<MsgTemplateContent> list = contentMapper.selectList(new QueryWrapper<MsgTemplateContent>()
                .eq(MsgTemplateContent.COL_TEMPLATE_ID, templateId)
                .eq(MsgTemplateContent.COL_DELETE_TIME, 0)
                .orderByDesc(MsgTemplateContent.COL_SHOW_ORDER)
                .last("LIMIT 1"));
        if (list.isEmpty()) return 0;
        return list.get(0).getShowOrder();
    }

    private Page<MsgTemplateDTO> toDtoPage(Page<MsgTemplate> page) {
        Set<String> userIds = page.getRecords().stream()
                .flatMap(t -> java.util.stream.Stream.of(t.getCreateUser(), t.getUpdateUser()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, String> userNameMap = nameCacheService.getUserNameMap(userIds);
        Page<MsgTemplateDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(t -> {
            MsgTemplateDTO dto = BeanCopyUtils.copy(t, MsgTemplateDTO.class);
            dto.setCreateUserName(userNameMap.getOrDefault(t.getCreateUser(), t.getCreateUser()));
            dto.setUpdateUserName(userNameMap.getOrDefault(t.getUpdateUser(), t.getUpdateUser()));
            return dto;
        }).collect(Collectors.toList()));
        return dtoPage;
    }

    private MsgTemplateDTO toDto(MsgTemplate template) {
        MsgTemplateDTO dto = BeanCopyUtils.copy(template, MsgTemplateDTO.class);
        dto.setCreateUserName(nameCacheService.getUserName(template.getCreateUser()));
        dto.setUpdateUserName(nameCacheService.getUserName(template.getUpdateUser()));
        return dto;
    }

    private MsgTemplateContentDTO toContentDto(MsgTemplateContent content) {
        MsgTemplateContentDTO dto = BeanCopyUtils.copy(content, MsgTemplateContentDTO.class);
        return dto;
    }
}
