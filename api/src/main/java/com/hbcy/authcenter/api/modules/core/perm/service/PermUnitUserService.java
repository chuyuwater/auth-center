package com.hbcy.authcenter.api.modules.core.perm.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourcePermMapper;
import com.hbcy.authcenter.api.modules.core.app.dto.GrantAppDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResPermDTO;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.service.OrgTreeService;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitMapper;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitUserMapper;
import com.hbcy.authcenter.api.modules.core.perm.dto.UnitUserDTO;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnit;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitUser;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserUpdateVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUserGrantVO;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppResourceMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import com.hbcy.authcenter.api.modules.core.user.dao.UserOrgMapper;
import com.hbcy.authcenter.api.modules.core.user.model.UserOrg;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.db.model.PageRespEx;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Transactional(rollbackFor = Exception.class)
    public void addUsersToUnit(PermUnitUserUpdateVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        String currentUserId = UserContextUtils.getUserId();
        PermUnit permUnit = permUnitMapper.selectById(vo.getUnitId());
        if (permUnit == null) {
            throw new ParamError("权限单元已被删除");
        }
        if (!permUnit.getTenantId().equals(tenantId)) {
            throw new PermissionError("权限单元不属于当前租户");
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
        for (PermUserGrantVO g : vo.getUserList()) {
            if (!userOrgSet.containsKey(g.getUserId()) || !userOrgSet.get(g.getUserId()).contains(g.getOrgId())) {
                throw new ParamError("用户或组织信息错误");
            }
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
        return resPermDTOS.stream().filter(
                resPermDTO -> resPermDTO.getResId().equals(resId)).toList();
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
}

