package com.hbcy.authcenter.api.modules.core.perm.service;

import cn.idev.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.core.app.dto.ResPermDTO;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.service.OrgTreeService;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitUserMapper;
import com.hbcy.authcenter.api.modules.core.perm.dto.UnitUserDTO;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitUser;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserUpdateVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUserGrantVO;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermUnitUserService extends ServiceImpl<PermUnitUserMapper, PermUnitUser> {

    @Resource
    private OrgTreeService orgTreeService;
    @Resource
    private TenantAppMapper tenantAppMapper;

    @Transactional(rollbackFor = Exception.class)
    public void addUsersToUnit(PermUnitUserUpdateVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        String currentUserId = UserContextUtils.getUserId();
        List<PermUnitUser> list = new ArrayList<>();
        for (PermUserGrantVO g : vo.getUserList()) {
            PermUnitUser u = new PermUnitUser();
            u.setId(UlidCreator.getUlid().toString());
            u.setUnitId(vo.getUnitId());
            u.setUserId(g.getUserId());
            u.setOrgId(g.getOrgId());
            u.setTenantId(tenantId);
            u.setCreateUser(currentUserId);
            u.setUpdateUser(currentUserId);
            list.add(u);
        }
        baseMapper.insertIgnore(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeUsersFromUnit(String unitId, List<String> userIds, String orgId) {
        remove(new QueryWrapper<PermUnitUser>()
                .eq(PermUnitUser.COL_UNIT_ID, unitId)
                .in(PermUnitUser.COL_USER_ID, userIds)
                .eq(PermUnitUser.COL_ORG_ID, orgId)
        );
    }

    /**
     * 获取用户在当前上下文的所有权限点
     */
    public List<ResPermDTO> listPerms(String appId) {
        String userId = UserContextUtils.getUserId();
        String tenantId = UserContextUtils.getTenantId();
        if (StringUtils.isBlank(appId)) {
            appId = UserContextUtils.getAppId();
        }
        //如果是默认管理员，直接获取app的最大权限
        if (UserContextUtils.isTenantAdmin()) {
            TenantApp tenantApp = tenantAppMapper.selectOne(new QueryWrapper<TenantApp>()
                    .eq(TenantApp.COL_APP_ID, appId)
                    .eq(TenantApp.COL_TENANT_ID, tenantId)
            );
            if (tenantApp == null) {
                return new ArrayList<>();
            }
            if (tenantApp.getGrantAll() > 0) {
                return baseMapper.listAppPerms(appId);
            } else {
                if (CollectionUtils.isEmpty(tenantApp.getPermIds())) {
                    return new ArrayList<>();
                }
                return baseMapper.listTenantAppMaxPerms(tenantApp.getPermIds());
            }
        }
        String orgId = UserContextUtils.getUserOrg();
        OrgTree orgNode = orgTreeService.getById(orgId);
        if (orgNode == null) {
            throw new ParamError("组织已被删除");
        }
        String idPath = orgNode.getIdPath();
        return baseMapper.listFilteredPerms(userId, appId, tenantId, idPath);
    }

    //移除角色授权人员
    public void deleteGrant(List<String> grantIds) {
        String tenantId = UserContextUtils.getTenantId();
        Set<String> toDelete = baseMapper.selectList(new QueryWrapper<PermUnitUser>()
                        .eq(PermUnitUser.COL_TENANT_ID, tenantId)
                        .in(PermUnitUser.COL_ID, grantIds)).stream().map(PermUnitUser::getId)
                .collect(Collectors.toSet());
        baseMapper.deleteByIds(toDelete);
    }

    //获取角色关联的人
    public List<UnitUserDTO> listGrantUsers(PermUnitUserQueryVO vo) {
        return baseMapper.listGrantUsers(vo);
    }
}

