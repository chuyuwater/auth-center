package com.hbcy.authcenter.api.modules.core.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.common.bean.EventDispatcher;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.common.enums.OrgNodeCategoryEnum;
import com.hbcy.authcenter.api.common.enums.OrgNodeTypeEnum;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import com.hbcy.authcenter.api.modules.core.tenant.utils.TenantIdUtils;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantForbiddenVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantInsertVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantQueryVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantUpdateVO;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.core.user.service.UserService;
import com.hbcy.authcenter.global.constants.EventConstants;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.base.util.BeanCopyUtils;
import com.hbcy.common.db.model.PageRespEx;
import com.hbcy.common.redis.RedisIdGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 姚泰然
 * @date 2025-12-23 14:38
 */
@Service
public class TenantService extends ServiceImpl<TenantMapper, Tenant> {

    public static final String TENANT_KEY = "portal:tenant:";
    @Resource
    private RedisIdGenerator redisIdGenerator;
    @Resource
    private TenantAppMapper tenantAppMapper;
    @Resource
    private OrgTreeMapper orgTreeMapper;
    @Resource
    private UserService userService;
    @Resource
    private EventDispatcher eventDispatcher;

    private void checkExist(String nameCn) {
        Tenant one = this.getOne(new QueryWrapper<Tenant>().eq(Tenant.COL_NAME_CN, nameCn), false);
        if (one != null) {
            throw new ParamError("租户名称已存在");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Tenant create(TenantInsertVO vo) {
        checkExist(vo.getNameCn());
        //租户不能被物理删除，所以用count就行，根租户id是0，不计入总数
        String calcedId = redisIdGenerator.generateId(TENANT_KEY, this::count, TenantIdUtils::convertToTitle);
        Tenant tenant = new Tenant();
        tenant.setId(calcedId);
        BeanCopyUtils.copy(vo, tenant);
        tenant.setCreateUser(UserContextUtils.getUserId());
        tenant.setUpdateUser(UserContextUtils.getUserId());
        //初始化组织树虚拟根节点
        OrgTree root = new OrgTree();
        root.setId(OrgTree.ORG_ID_TEMPLATE.formatted(tenant.getId(), 0));
        root.setNodeName("根组织");
        root.setNodeType(OrgNodeTypeEnum.ORG.getValue());
        root.setNodeCategory(OrgNodeCategoryEnum.CORP.getValue());
        root.setExistType(OrgTree.EXIST_TYPE_VIRTUAL);
        root.setCreateUser(UserContextUtils.getUserId());
        root.setUpdateUser(UserContextUtils.getUserId());
        root.setIdPath(root.getId());
        root.setTenantId(tenant.getId());
        root.setParentId("");
        root.setShowOrder(0);
        //租户名字作为真正的根节点(默认组织树，行政组织）
        OrgTree current = new OrgTree();
        current.setId(OrgTree.ORG_ID_TEMPLATE.formatted(tenant.getId(), 1));
        current.setNodeName(tenant.getNameCn());
        current.setShortName(tenant.getShortName());
        current.setNodeType(OrgNodeTypeEnum.ORG.getValue());
        current.setShowOrder(0);
        if (tenant.getNameCn().contains("集团")) {
            current.setNodeCategory(OrgNodeCategoryEnum.CORP.getValue());
        } else {
            current.setNodeCategory(OrgNodeCategoryEnum.COMPANY.getValue());
        }
        current.setParentId(root.getId());
        current.setCreateUser(UserContextUtils.getUserId());
        current.setUpdateUser(UserContextUtils.getUserId());
        current.setIdPath(root.getId() + G.ID_PATH_SPLITTER + current.getId());
        current.setTenantId(tenant.getId());
        //租户管理员
        User user = new User();
        user.setId(UlidCreator.getUlid().toString());
        user.setAccount(vo.getContactPhone());
        user.setRealName(vo.getContactUser());
        user.setPhone(vo.getContactPhone());
        tenant.setAdminId(user.getId());
        try {
            baseMapper.insert(tenant);
            orgTreeMapper.insert(root);
            orgTreeMapper.insert(current);
            userService.createUser(user, current);
        } catch (DuplicateKeyException e) {
            throw new ParamError("请重试");
        }
        eventDispatcher.dispatch(EventConstants.TENANT_CREATED, tenant);
        return tenant;
    }

    /**
     * 更新租户基本信息
     *
     * @param vo 基本信息
     * @param id 租户id
     * @return 更新后的租户信息
     */
    public Tenant update(TenantUpdateVO vo, String id) {
        Tenant tenant = getById(id);
        if (tenant == null) {
            throw new ParamError("指定租户不存在");
        }
        BeanCopyUtils.copy(vo, tenant);
        tenant.setUpdateUser(UserContextUtils.getUserId());
        try {
            this.updateById(tenant);
        } catch (DuplicateKeyException e) {
            throw new ParamError("租户名称重复");
        }
        return tenant;
    }

    @Transactional(rollbackFor = Exception.class)
    public void switchStatus(TenantForbiddenVO vo) {
        if (G.DEFAULT_TENANT.equals(vo.getTenantId()) && vo.getForbidden() == 1) {
            throw new ParamError("系统租户不能被禁用");
        }
        Tenant tenant = getById(vo.getTenantId());
        if (tenant == null) {
            throw new ParamError("指定租户不存在");
        }
        if (tenant.getForbidden().equals(vo.getForbidden())) {
            return;
        }
        Tenant toUpdate = new Tenant();
        toUpdate.setId(vo.getTenantId());
        toUpdate.setForbidden(vo.getForbidden());
        toUpdate.setUpdateUser(UserContextUtils.getUserId());
        //应用授权状态级联变化
        tenantAppMapper.switchTenantStatus(
                vo.getForbidden(), vo.getTenantId(), UserContextUtils.getUserId());
        this.updateById(tenant);
    }

    public PageResp<Tenant> list(TenantQueryVO vo) {
        Page<Tenant> dbPage = vo.getDbPage();
        Page<Tenant> resp = baseMapper.selectPage(dbPage, new QueryWrapper<Tenant>()
                .eq(vo.getForbidden() != null, Tenant.COL_FORBIDDEN, vo.getForbidden())
                .and(StringUtils.isNotBlank(vo.getKeyword()),
                        qw -> qw.like(Tenant.COL_NAME_CN, vo.getKeyword())
                                .or()
                                .like(Tenant.COL_ID, vo.getKeyword()))
        );
        return new PageRespEx<>(resp);
    }

    public void delete(String id) {
        if (G.DEFAULT_TENANT.equals(id)) {
            throw new ParamError("请勿删除系统租户");
        }
        Tenant tenant = getById(id);
        if (tenant == null) {
            return;
        }
        var anyApp = tenantAppMapper.exists(new QueryWrapper<TenantApp>()
                .eq(TenantApp.COL_TENANT_ID, tenant.getId()));
        if (anyApp) {
            throw new ParamError("请先删除该租户下的所有应用");
        }
        Long cnt = orgTreeMapper.selectCount(new QueryWrapper<OrgTree>()
                .eq(OrgTree.COL_TENANT_ID, tenant.getId()));
        if (cnt > 1) { //不含虚拟根节点
            throw new ParamError("请先删除该租户下的所有组织");
        }
        tenant.setUpdateUser(UserContextUtils.getUserId());
        //逻辑删除
        this.removeById(tenant);
    }
}
