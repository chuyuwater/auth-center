package com.hbcy.authcenter.api.modules.minor.msg.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.common.bean.NameCacheService;
import com.hbcy.authcenter.api.modules.minor.msg.dto.SelectRuleDTO;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSeqGenerator;
import com.hbcy.authcenter.api.modules.minor.msg.dao.SelectRuleConditionMapper;
import com.hbcy.authcenter.api.modules.minor.msg.dao.SelectRuleMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.SelectRule;
import com.hbcy.authcenter.api.modules.minor.msg.model.SelectRuleCondition;
import com.hbcy.authcenter.api.modules.minor.msg.vo.RuleConditionVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.RuleCreateVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.RuleQueryVO;
import com.hbcy.common.base.error.ClientError;
import com.hbcy.common.base.json.JsonUtils;
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

@Service
public class SelectRuleService extends ServiceImpl<SelectRuleMapper, SelectRule> {
    @Resource
    private MsgSeqGenerator msgSeqGenerator;
    @Resource
    private SelectRuleConditionMapper conditionMapper;
    @Resource
    private NameCacheService nameCacheService;

    @Transactional(rollbackFor = Exception.class)
    public SelectRuleDTO createRule(RuleCreateVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        String userId = UserContextUtils.getUserId();

        SelectRule rule = BeanCopyUtils.copy(vo, SelectRule.class);
        rule.setId(UlidCreator.getUlid().toString());
        rule.setRuleNo(msgSeqGenerator.generate(tenantId, MsgSeqGenerator.TYPE_RULE));
        rule.setForbidden(0);
        rule.setRefCount(0);
        rule.setTenantId(tenantId);
        rule.setCreateUser(userId);
        rule.setUpdateUser(userId);
        baseMapper.insert(rule);

        // 保存条件
        if (vo.getConditions() != null) {
            int order = 0;
            for (RuleConditionVO condVO : vo.getConditions()) {
                SelectRuleCondition cond = buildCondition(rule.getId(), condVO, order++);
                conditionMapper.insert(cond);
            }
        }
        return toDto(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    public SelectRuleDTO updateRule(String id, RuleCreateVO vo) {
        SelectRule rule = getById(id);
        if (rule == null) {
            throw new ClientError("规则不存在");
        }
        if (vo.getRuleName() != null) rule.setRuleName(vo.getRuleName());
        if (vo.getBizEntity() != null) rule.setBizEntity(vo.getBizEntity());
        if (vo.getGroupId() != null) rule.setGroupId(vo.getGroupId());
        if (vo.getDescription() != null) rule.setDescription(vo.getDescription());
        rule.setUpdateUser(UserContextUtils.getUserId());
        baseMapper.updateById(rule);

        // 更新条件：先删后增
        if (vo.getConditions() != null) {
            conditionMapper.delete(new QueryWrapper<SelectRuleCondition>()
                    .eq(SelectRuleCondition.COL_RULE_ID, id));
            int order = 0;
            for (RuleConditionVO condVO : vo.getConditions()) {
                SelectRuleCondition cond = buildCondition(id, condVO, order++);
                conditionMapper.insert(cond);
            }
        }
        return toDto(rule);
    }

    public void toggleRule(String id) {
        SelectRule rule = getById(id);
        if (rule == null) {
            throw new ClientError("规则不存在");
        }
        rule.setForbidden(rule.getForbidden() == 0 ? 1 : 0);
        rule.setUpdateUser(UserContextUtils.getUserId());
        baseMapper.updateById(rule);
    }

    public void deleteRule(String id) {
        baseMapper.update(new UpdateWrapper<SelectRule>()
                .eq(SelectRule.COL_ID, id)
                .set(SelectRule.COL_DELETE_TIME, System.currentTimeMillis())
                .set(SelectRule.COL_UPDATE_USER, UserContextUtils.getUserId()));
        conditionMapper.delete(new QueryWrapper<SelectRuleCondition>()
                .eq(SelectRuleCondition.COL_RULE_ID, id));
    }

    public PageResp<SelectRuleDTO> queryRule(RuleQueryVO vo) {
        Page<SelectRule> dbPage = vo.getDbPage();
        String tenantId = UserContextUtils.getTenantId();
        QueryWrapper<SelectRule> qw = new QueryWrapper<SelectRule>()
                .eq(SelectRule.COL_TENANT_ID, tenantId)
                .eq(SelectRule.COL_DELETE_TIME, 0)
                .eq(StringUtils.isNotBlank(vo.getBizEntity()), SelectRule.COL_BIZ_ENTITY, vo.getBizEntity())
                .eq(StringUtils.isNotBlank(vo.getGroupId()), SelectRule.COL_GROUP_ID, vo.getGroupId())
                .and(StringUtils.isNotBlank(vo.getKeyword()),
                        w -> w.like(SelectRule.COL_RULE_NAME, vo.getKeyword())
                                .or().like(SelectRule.COL_RULE_NO, vo.getKeyword()))
                .orderByDesc(SelectRule.COL_CREATE_TIME);
        Page<SelectRule> page = baseMapper.selectPage(dbPage, qw);
        Set<String> userIds = page.getRecords().stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getCreateUser(), r.getUpdateUser()))
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<String, String> userNameMap = nameCacheService.getUserNameMap(userIds);
        Page<SelectRuleDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(r -> {
            SelectRuleDTO dto = BeanCopyUtils.copy(r, SelectRuleDTO.class);
            dto.setCreateUserName(userNameMap.getOrDefault(r.getCreateUser(), r.getCreateUser()));
            dto.setUpdateUserName(userNameMap.getOrDefault(r.getUpdateUser(), r.getUpdateUser()));
            return dto;
        }).collect(Collectors.toList()));
        return new PageRespEx<>(dtoPage);
    }

    public SelectRuleDTO getRuleDetail(String id) {
        SelectRule rule = getById(id);
        if (rule == null) {
            throw new ClientError("规则不存在");
        }
        return toDto(rule);
    }

    public List<SelectRuleCondition> listConditions(String ruleId) {
        return conditionMapper.selectByRuleId(ruleId);
    }

    /**
     * 根据规则ID计算目标用户列表
     * 条件间取交集
     */
    public Set<String> computeTargetUsers(String ruleId) {
        List<SelectRuleCondition> conditions = conditionMapper.selectByRuleId(ruleId);
        Set<String> result = null;
        for (SelectRuleCondition cond : conditions) {
            Set<String> users = resolveCondition(cond);
            if (result == null) {
                result = users;
            } else {
                result.retainAll(users);
            }
        }
        return result != null ? result : Set.of();
    }

    private Set<String> resolveCondition(SelectRuleCondition cond) {
        // TODO: 复用 UserService.filterUser4Select 的底层能力
        // 目前返回空集合，等待对接
        return Set.of();
    }

    private SelectRuleCondition buildCondition(String ruleId, RuleConditionVO vo, int order) {
        SelectRuleCondition cond = new SelectRuleCondition();
        cond.setId(UlidCreator.getUlid().toString());
        cond.setRuleId(ruleId);
        cond.setCondType(vo.getCondType());
        cond.setCondOp(vo.getCondOp() != null ? vo.getCondOp() : "in");
        cond.setCondValues(JsonUtils.toJsonStr(vo.getCondValues()));
        cond.setShowOrder(vo.getShowOrder() != null ? vo.getShowOrder() : order);
        return cond;
    }

    private SelectRuleDTO toDto(SelectRule rule) {
        SelectRuleDTO dto = BeanCopyUtils.copy(rule, SelectRuleDTO.class);
        dto.setCreateUserName(nameCacheService.getUserName(rule.getCreateUser()));
        dto.setUpdateUserName(nameCacheService.getUserName(rule.getUpdateUser()));
        return dto;
    }
}
