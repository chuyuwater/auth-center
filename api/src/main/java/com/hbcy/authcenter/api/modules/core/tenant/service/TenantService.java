package com.hbcy.authcenter.api.modules.core.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.tenant.utils.TenantIdUtils;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantForbiddenVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantQueryVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantUpsertVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.db.model.PageResp;
import com.hbcy.common.redis.RedisIdGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
    private OrgTreeMapper orgTreeMapper;

    private void checkExist(String nameCn) {
        Tenant one = this.getOne(new QueryWrapper<Tenant>().eq(Tenant.COL_NAME_CN, nameCn), false);
        if (one != null) {
            throw new ParamError("租户名称已存在");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Tenant create(TenantUpsertVO vo) {
        checkExist(vo.getNameCn());
        //租户不能被物理删除，所以用count就行
        String calcedId = redisIdGenerator.generateId(TENANT_KEY, this::count, TenantIdUtils::convertToTitle);
        Tenant tenant = new Tenant();
        tenant.setId(calcedId);
        BeanUtils.copyProperties(vo, tenant);
        tenant.setCreateUser(UserContextUtils.getUserId());
        tenant.setUpdateUser(UserContextUtils.getUserId());
        try {
            this.save(tenant);
        } catch (DuplicateKeyException e) {
            throw new ParamError("请重试");
        }
        //初始化组织树虚拟根节点
        OrgTree root = new OrgTree();
        root.setId(OrgTree.ORG_ID_TEMPLATE.formatted(tenant.getId(), 0));
        root.setNodeName("根组织");
        root.setCreateUser(UserContextUtils.getUserId());
        root.setUpdateUser(UserContextUtils.getUserId());
        root.setIdPath(root.getId());
        root.setTenantId(tenant.getId());
        //租户名字作为真正的根节点(默认组织树，行政组织）
        OrgTree current = new OrgTree();
        current.setId(OrgTree.ORG_ID_TEMPLATE.formatted(tenant.getId(), 1));
        current.setNodeName(tenant.getNameCn());
        current.setShortName(tenant.getShortName());
        current.setParentId(root.getId());
        current.setCreateUser(UserContextUtils.getUserId());
        current.setUpdateUser(UserContextUtils.getUserId());
        current.setIdPath(root.getId() + G.ID_PATH_SPLITTER + current.getId());
        try {
            orgTreeMapper.append(root);
            orgTreeMapper.append(current);
        } catch (DuplicateKeyException e) {
            throw new ParamError("请重试");
        }
        return tenant;
    }

    public Tenant update(TenantUpsertVO vo, String id) {
        Tenant tenant = getById(id);
        if (tenant == null) {
            throw new ParamError("指定租户不存在");
        }
        BeanUtils.copyProperties(vo, tenant);
        tenant.setUpdateUser(UserContextUtils.getUserId());
        try {
            this.updateById(tenant);
        } catch (DuplicateKeyException e) {
            throw new ParamError("租户名称重复");
        }
        return tenant;
    }

    public void switchStatus(TenantForbiddenVO vo) {
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
        this.updateById(tenant);
    }

    public PageResp<Tenant> list(TenantQueryVO vo) {
        Page<Tenant> dbPage = vo.getDbPage();
        Page<Tenant> resp = baseMapper.selectPage(dbPage, new QueryWrapper<Tenant>()
                .eq(vo.getForbidden() != null, Tenant.COL_FORBIDDEN, vo.getForbidden())
                .or(StringUtils.isNotBlank(vo.getName()))
                //模糊查询，租户数量不会多，无需考虑优化
                .like(Tenant.COL_NAME_CN, vo.getName())
                .like(Tenant.COL_SHORT_NAME, vo.getName())
        );
        return new PageResp<>(resp);
    }

    public void delete(String id) {
        Tenant tenant = getById(id);
        if (tenant == null) {
            return;
        }
        tenant.setUpdateUser(UserContextUtils.getUserId());
        //逻辑删除
        this.removeById(tenant);
    }
}
