package com.hbcy.authcenter.api.modules.core.perm.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourcePermMapper;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourceTreeMapper;
import com.hbcy.authcenter.api.modules.core.app.dto.GrantAppDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResPermDTO;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
import com.hbcy.authcenter.api.modules.core.inner.service.InnerService;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.service.OrgTreeService;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitMapper;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitUserMapper;
import com.hbcy.authcenter.api.modules.core.perm.dto.UnitUserDTO;
import com.hbcy.authcenter.api.modules.core.perm.dto.UserUnitDTO;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnit;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitUser;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserUpdateVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUserGrantVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUserUnitQueryVO;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppResourceMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import com.hbcy.authcenter.api.modules.core.user.dao.UserOrgMapper;
import com.hbcy.authcenter.api.modules.core.user.model.UserOrg;
import com.hbcy.authcenter.gateway.vo.RefreshUserPermVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.db.model.PageRespEx;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermUnitUserService extends ServiceImpl<PermUnitUserMapper, PermUnitUser> {

    @Resource
    private OrgTreeService orgTreeService;
    @Resource
    private TenantAppMapper tenantAppMapper;
    @Resource
    private TenantAppResourceMapper tenantAppResourceMapper;
    @Resource
    private UserOrgMapper userOrgMapper;
    @Resource
    private PermUnitMapper permUnitMapper;
    @Resource
    private ResourcePermMapper resourcePermMapper;
    @Resource
    private InnerService innerService;
    @Resource
    private ResourceTreeMapper resourceTreeMapper;

    @Transactional(rollbackFor = Exception.class)
    public void addUsersToUnits(PermUnitUserUpdateVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        String currentUserId = UserContextUtils.getUserId();
        List<PermUnit> permUnitList = permUnitMapper.selectList(new QueryWrapper<PermUnit>()
                .select(PermUnit.COL_ID)
                .in(PermUnit.COL_ID, vo.getUnitIdList())
                .eq(PermUnit.COL_TENANT_ID, tenantId));
        if (permUnitList.size() < vo.getUnitIdList().size()) {
            throw new ParamError("权限单元选择错误");
        }
        //校验数据，在user_org表里找到对应
        //为减少io，直接取出用户所有的org整理成map
        Set<String> userIds = vo.getUserList().stream().map(
                PermUserGrantVO::getUserId).collect(Collectors.toSet());
        List<UserOrg> userOrgs = userOrgMapper.selectList(new QueryWrapper<UserOrg>()
                .in(UserOrg.COL_USER_ID, userIds)
                .eq(UserOrg.COL_TENANT_ID, tenantId));
        Map<String, Set<String>> userOrgSet = userOrgs.stream().collect(Collectors.groupingBy(
                UserOrg::getUserId,
                Collectors.mapping(UserOrg::getOrgId, Collectors.toSet())
        ));
        List<PermUnitUser> list = new ArrayList<>();
        for (PermUnit permUnit : permUnitList) {
            for (PermUserGrantVO g : vo.getUserList()) {
                if (!userOrgSet.containsKey(g.getUserId()) || !userOrgSet.get(g.getUserId()).contains(g.getOrgId())) {
                    throw new ParamError("用户或组织信息错误");
                }
                PermUnitUser u = new PermUnitUser();
                u.setId(UlidCreator.getUlid().toString());
                u.setUnitId(permUnit.getId());
                u.setUserId(g.getUserId());
                u.setOrgId(g.getOrgId());
                u.setTenantId(tenantId);
                u.setCreateUser(currentUserId);
                u.setUpdateUser(currentUserId);
                list.add(u);
            }
        }
        baseMapper.insertIgnore(list);
    }

    public void removeUsersFromUnit(PermUnitUserUpdateVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        baseMapper.batchDelete(vo, tenantId);
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
        //先确认应用有授权
        TenantApp tenantApp = tenantAppMapper.selectOne(new QueryWrapper<TenantApp>()
                .eq(TenantApp.COL_APP_ID, appId)
                .eq(TenantApp.COL_TENANT_ID, tenantId)
                .eq(TenantApp.COL_FORBIDDEN, 0)
        );
        if (tenantApp == null) {
            return new ArrayList<>();
        }
        //如果是默认管理员，直接获取app的最大权限
        if (UserContextUtils.isTenantAdmin()) {
            if (tenantApp.getGrantAll() > 0) {
                return resourcePermMapper.listAppPerms(appId);
            } else {
                Set<String> grantedPermIds = tenantAppResourceMapper.getGrantedPermIds(tenantId, appId);
                if (grantedPermIds.isEmpty()) {
                    return new ArrayList<>();
                }
                return baseMapper.listPermInfo(grantedPermIds);
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

    public List<ResPermDTO> listPerms(String appId, String resId) {
        List<ResPermDTO> resPermDTOS = listPerms(appId);
        if (StringUtils.isBlank(resId)) {
            return resPermDTOS;
        }
        return resPermDTOS.stream().filter(
                resPermDTO -> resPermDTO.getResId().equals(resId)).toList();
    }

    public List<ResPermDTO> listPermByCustomId(String appId, String customId) {
        if (StringUtils.isBlank(customId)) {
            return listPerms(appId);
        }
        ResourceTree node = resourceTreeMapper.selectOne(new QueryWrapper<ResourceTree>()
                .eq(ResourceTree.COL_APP_ID, appId)
                .eq(ResourceTree.COL_CUSTOM_ID, customId));
        if (node == null) {
            return List.of();
        }
        return listPerms(appId, node.getId());
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
    public PageResp<UnitUserDTO> listGrantUsers(PermUnitUserQueryVO vo) {
        Page<Object> dbPage = vo.getDbPage();
        Page<UnitUserDTO> page = baseMapper.listGrantUsers(dbPage, vo);
        return new PageRespEx<>(page);
    }

    //获取当前用户有权访问的app列表
    public List<GrantAppDTO> listApp(String orgId, boolean withForbidden) {
        String userId = UserContextUtils.getUserId();
        if (UserContextUtils.isTenantAdmin()) {
            //租户管理员直接获取授权的非禁用app列表
            List<GrantAppDTO> apps = tenantAppMapper.listGrantApps(UserContextUtils.getTenantId());
            if (withForbidden) {
                return new ArrayList<>(apps);
            }
            List<GrantAppDTO> resp = new ArrayList<>();
            for (GrantAppDTO app : apps) {
                if (app.getForbidden() == 0) {
                    resp.add(app);
                }
            }
            return resp;
        }
        if (StringUtils.isBlank(orgId)) {
            orgId = UserContextUtils.getUserOrg();
        }
        OrgTree org = orgTreeService.getById(orgId);
        if (org == null) {
            throw new ParamError("用户未加入任何组织");
        }
        return baseMapper.listGrantApps(userId, orgId, withForbidden);
    }


    public Map<String, Boolean> checkPerm(String userId, String orgId, String appId, Collection<String> permCodes) {
        List<ResourcePerm> resourcePerms = resourcePermMapper.selectList(new QueryWrapper<ResourcePerm>()
                .eq(ResourcePerm.COL_APP_ID, appId)
                .in(ResourcePerm.COL_PERM_CODE, permCodes));
        //同一个APP下permCode是唯一的
        Map<String, Boolean> defaultResp = new HashMap<>();
        BiMap<String, String> permCodeIdMap = HashBiMap.create();
        for (String permCode : permCodes) {
            defaultResp.put(permCode, false);
        }
        if (resourcePerms.isEmpty()) {
            return defaultResp;
        }
        for (ResourcePerm rp : resourcePerms) {
            permCodeIdMap.put(rp.getPermCode(), rp.getId());
        }
        Map<String, Boolean> checked = checkFromCache(userId, orgId, permCodes, permCodeIdMap, defaultResp);
        if (checked != null) return checked;
        //刷新缓存
        innerService.refreshUserPerms(new RefreshUserPermVO()
                .setUserId(userId)
                .setOrgId(orgId)
                .setTenantId(UserContextUtils.getTenantId()));
        checked = checkFromCache(userId, orgId, permCodes, permCodeIdMap, defaultResp);
        if (checked != null) {
            return checked;
        }
        return defaultResp;
    }

    private @Nullable Map<String, Boolean> checkFromCache(String userId, String orgId,
                                                          Collection<String> permCodes,
                                                          BiMap<String, String> permCodeIdMap,
                                                          Map<String, Boolean> defaultResp) {
        Map<String, Boolean> checked = innerService.checkPerms(userId, orgId, permCodeIdMap.values());
        if (checked != null) {
            for (String permCode : permCodes) {
                defaultResp.put(permCode, checked.getOrDefault(permCodeIdMap.get(permCode), false));
            }
            return checked;
        }
        return null;
    }

    public boolean checkPerm(String permCode) {
        String appId = UserContextUtils.getAppId();
        String userId = UserContextUtils.getUserId();
        String orgId = UserContextUtils.getUserOrg();
        return checkPerm(userId, orgId, appId, List.of(permCode)).getOrDefault(
                permCode, false);
    }

    /**
     * 查询用户关联的权限单元
     * @param vo 查询条件
     * @return 满足条件的权限单元列表
     */
    public List<UserUnitDTO> listUserUnits(PermUserUnitQueryVO vo) {
        vo.setTenantId(UserContextUtils.getTenantId());
        return baseMapper.listUserUnits(vo);
    }
}

