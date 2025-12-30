package com.hbcy.authcenter.api.modules.core.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import com.hbcy.authcenter.api.modules.core.tenant.utils.TenantIdUtils;
import com.hbcy.authcenter.api.modules.core.tenant.vo.*;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.core.user.service.UserService;
import com.hbcy.authcenter.api.modules.core.user.vo.UserCreateVO;
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

    private void checkExist(String nameCn) {
        Tenant one = this.getOne(new QueryWrapper<Tenant>().eq(Tenant.COL_NAME_CN, nameCn), false);
        if (one != null) {
            throw new ParamError("租户名称已存在");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Tenant create(TenantInsertVO vo) {
        checkExist(vo.getNameCn());
        //租户不能被物理删除，所以用count就行
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
        //创建租户管理员
        UserCreateVO userCreateVO = new UserCreateVO();
        userCreateVO.setNodeId(current.getId());
        userCreateVO.setAccount(vo.getContactPhone());
        userCreateVO.setRealName(vo.getContactUser());
        userCreateVO.setPhone(vo.getContactPhone());
        String userId = userService.createUser(userCreateVO);
        tenant.setAdminId(userId);
        try {
            this.save(tenant);
            orgTreeMapper.append(root);
            orgTreeMapper.append(current);
        } catch (DuplicateKeyException e) {
            throw new ParamError("请重试");
        }
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

    /**
     * 修改默认管理员信息
     * 供租户使用
     *
     * @param vo 新的管理员
     */
    public void updateDefaultAdmin(TenantAdminUpdateVO vo) {
        User user = userService.getById(vo.getUserId());
        String tenantId = UserContextUtils.getTenantId();
        if (!user.getTenantId().equals(tenantId)) {
            throw new ParamError("用户不属于当前租户");
        }
        Tenant toUpdate = new Tenant();
        toUpdate.setId(tenantId);
        toUpdate.setAdminId(user.getId());
        toUpdate.setContactPhone(user.getPhone());
        toUpdate.setContactUser(user.getRealName());
        toUpdate.setUpdateUser(UserContextUtils.getUserId());
        save(toUpdate);
    }

    @Transactional(rollbackFor = Exception.class)
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
        //应用授权状态级联变化
        if (vo.getForbidden() == 1)
            tenantAppMapper.switchTenantStatus(
                    vo.getForbidden(), vo.getTenantId(), UserContextUtils.getUserId());
        this.updateById(tenant);
    }

    public PageResp<Tenant> list(TenantQueryVO vo) {
        Page<Tenant> dbPage = vo.getDbPage();
        Page<Tenant> resp = baseMapper.selectPage(dbPage, new QueryWrapper<Tenant>()
                .eq(vo.getForbidden() != null, Tenant.COL_FORBIDDEN, vo.getForbidden())
                .or(StringUtils.isNotBlank(vo.getKeyword()))
                //模糊查询，租户数量不会多，无需考虑优化
                .like(Tenant.COL_NAME_CN, vo.getKeyword())
                .like(Tenant.COL_ID, vo.getKeyword())
        );
        return new PageRespEx<>(resp);
    }

    public void delete(String id) {
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
