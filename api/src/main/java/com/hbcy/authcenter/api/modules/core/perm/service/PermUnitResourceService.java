package com.hbcy.authcenter.api.modules.core.perm.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.common.enums.OrgNodeCategoryEnum;
import com.hbcy.authcenter.api.common.enums.ResourceShowLevelEnum;
import com.hbcy.authcenter.api.modules.core.app.dto.GrantAppDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResPermDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResTreeDTO;
import com.hbcy.authcenter.api.modules.core.app.service.ResourcePermService;
import com.hbcy.authcenter.api.modules.core.app.service.ResourceTreeService;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourceTreeQueryVO;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitMapper;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitResourceMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnit;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitResource;
import com.hbcy.authcenter.api.modules.core.perm.vo.ClientResQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitAppDTO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitResourceQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitResourceSaveVO;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.tree.TreeNode;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermUnitResourceService extends ServiceImpl<PermUnitResourceMapper, PermUnitResource> {

    @Resource
    private ResourcePermService resourcePermService;
    @Resource
    private TenantAppMapper tenantAppMapper;
    @Resource
    private PermUnitMapper permUnitMapper;
    @Resource
    private PermUnitUserService permUnitUserService;
    @Resource
    private ResourceTreeService resourceTreeService;
    @Resource
    private OrgTreeMapper orgTreeMapper;

    @Transactional(rollbackFor = Exception.class)
    public void addCodesToUnit(PermUnitResourceSaveVO vo) {
        String userId = UserContextUtils.getUserId();
        String tenantId = UserContextUtils.getTenantId();
        //检查操作权限
        PermUnit permUnit = permUnitMapper.selectById(vo.getUnitId());
        if (permUnit == null) {
            throw new ParamError("权限单元已被删除");
        }
        if (!permUnit.getTenantId().equals(tenantId)) {
            throw new PermissionError();
        }
        //检查应用是否已授权
        Long cnt = tenantAppMapper.selectCount(new QueryWrapper<TenantApp>()
                .eq(TenantApp.COL_TENANT_ID, tenantId)
                .eq(TenantApp.COL_APP_ID, vo.getAppId()));
        if (cnt == 0) {
            throw new PermissionError();
        }
        //仅保留权限点
        Set<String> pointIds = resourcePermService.filterAppPermIds(vo.getAppId(), vo.getPermIds());
        //用户只能授权自己拥有的权限
        List<ResPermDTO> granted = permUnitUserService.listPerms(vo.getAppId());
        var grantedIds = granted.stream().map(ResPermDTO::getId).collect(Collectors.toSet());

        List<PermUnitResource> resources = new ArrayList<>();
        for (String pointId : pointIds) {
            if (!grantedIds.contains(pointId)) {
                throw new PermissionError();
            }
            PermUnitResource resource = new PermUnitResource();
            resource.setId(UlidCreator.getUlid().toString());
            resource.setUnitId(vo.getUnitId());
            resource.setPermId(pointId);
            resource.setAppId(vo.getAppId());
            resource.setCreateUser(userId);
            resource.setUpdateUser(userId);
            resource.setTenantId(tenantId);
            resources.add(resource);
        }
        //先全部删除，再插入
        baseMapper.delete(new QueryWrapper<PermUnitResource>()
                .eq(PermUnitResource.COL_APP_ID, vo.getAppId())
                .eq(PermUnitResource.COL_UNIT_ID, vo.getUnitId()));
        baseMapper.insert(resources);
    }

    public List<PermUnitAppDTO> listApps(String unitId) {
        PermUnit permUnit = permUnitMapper.selectById(unitId);
        if (permUnit == null) {
            throw new ParamError("指定权限单元已被删除");
        }
        String tenantId = UserContextUtils.getTenantId();
        if (!permUnit.getTenantId().equals(tenantId)) {
            throw new PermissionError();
        }
        //当前租户已授权的所有app
        List<GrantAppDTO> apps = tenantAppMapper.listGrantApps(tenantId);
        //当前unit已关联的所有app
        Set<String> unitApps = baseMapper.listGrantedApps(unitId);
        List<PermUnitAppDTO> resp = new ArrayList<>();
        for (GrantAppDTO app : apps) {
            if (app.getForbidden() != 0) {
                continue;
            }
            resp.add(new PermUnitAppDTO()
                    .setAppName(app.getNameCn())
                    .setAppMemo(app.getMemo())
                    .setAppId(app.getAppId())
                    .setPacked(unitApps.contains(app.getAppId())));
        }
        return resp;
    }

    public List<TreeNode<ResTreeDTO>> listUnitResources(PermUnitResourceQueryVO vo) {
        //查找权限单元已封装的权限
        PermUnit permUnit = permUnitMapper.selectById(vo.getUnitId());
        if (permUnit == null) {
            return new ArrayList<>();
        }
        if (!permUnit.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        List<ResPermDTO> currentUserPerms = permUnitUserService.listPerms(vo.getAppId());
        ResourceTreeQueryVO queryVO = new ResourceTreeQueryVO();
        queryVO.setAppId(vo.getAppId());
        queryVO.setWithPerm(true);
        TreeNode<ResTreeDTO> tree = resourceTreeService.listResTreeRecursively(
                queryVO, currentUserPerms, vo.isOnlyPacked());
        return tree.getChildren();
    }

    /**
     * 获取上下文的资源树
     *
     * @return 移除了未授权资源
     */
    @Cacheable(value = "@1m")
    public List<TreeNode<ResTreeDTO>> listUserResources(ClientResQueryVO vo) {
        OrgTree orgTree = orgTreeMapper.selectById(vo.getOrgId());
        boolean isPrj = orgTree.getNodeCategory().equals(OrgNodeCategoryEnum.PROJECT.getValue());
        List<ResPermDTO> currentUserPerms = permUnitUserService.listPerms(vo.getAppId());
        if (CollectionUtils.isEmpty(currentUserPerms)) {
            return new ArrayList<>();
        }
        ResourceTreeQueryVO queryVO = new ResourceTreeQueryVO();
        queryVO.setAppId(vo.getAppId());
        queryVO.setClientType(vo.getClientType());
        queryVO.setWithPerm(vo.isWithPerm());
        queryVO.setWithHidden(false);
        queryVO.setShowLevel(isPrj ? ResourceShowLevelEnum.PRJ.getValue() : ResourceShowLevelEnum.ORG.getValue());
        TreeNode<ResTreeDTO> root = resourceTreeService.listResTreeRecursively(
                queryVO, currentUserPerms, true);
        return root.getChildren();
    }
}

