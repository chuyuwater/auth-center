package com.hbcy.authcenter.api.modules.core.perm.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.google.common.base.Splitter;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.common.enums.OrgNodeCategoryEnum;
import com.hbcy.authcenter.api.common.enums.ResourceShowLevelEnum;
import com.hbcy.authcenter.api.common.enums.TreeQueryLevelEnum;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourcePermMapper;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourceTreeMapper;
import com.hbcy.authcenter.api.modules.core.app.dto.GrantAppDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResPermDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResTreeDTO;
import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
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
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.tree.TreeNode;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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
    private ResourceTreeMapper resourceTreeMapper;
    @Resource
    private OrgTreeMapper orgTreeMapper;
    @Resource
    private ResourcePermMapper resourcePermMapper;

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
        //仅保留权限点
        Set<String> pointIds = resourcePermService.filterAppPermIds(vo.getAppId(), vo.getPermIds());
        if (CollectionUtils.isEmpty(pointIds)) {
            throw new ParamError("无有效权限");
        }
        //用户只能授权自己拥有的权限
        List<ResPermDTO> granted = permUnitUserService.listPerms(vo.getAppId());
        if (CollectionUtils.isEmpty(granted)) {
            throw new ParamError("无有效权限");
        }
        var grantedIds = granted.stream().map(ResPermDTO::getId).collect(
                Collectors.toSet());

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
            resp.add(new PermUnitAppDTO()
                    .setAppIcon(app.getIcon())
                    .setAppName(app.getNameCn())
                    .setAppMemo(app.getMemo())
                    .setAppId(app.getAppId())
                    .setPacked(unitApps.contains(app.getAppId()))
                    .setForbidden(app.getForbidden() != null ? app.getForbidden() : 0));
        }
        return resp;
    }

    /**
     * 获取权限单元关联的权限树
     * 这个接口有多个作用：
     * 1. 当onlyPacked为true时，则返回权限单元已封装的权限树
     * 2. 当onlyPacked为false时，则返回用户有权授权的权限树，同时标注出已授权的节点
     * 对不同的用户而言，这颗树是不一样的，用户只能看到自己本身拥有的权限。
     *
     * @param vo 查询条件
     * @return 权限树
     */
    public List<TreeNode<ResTreeDTO>> listUnitResources(PermUnitResourceQueryVO vo) {
        //查找权限单元已封装的权限
        PermUnit permUnit = permUnitMapper.selectById(vo.getUnitId());
        if (permUnit == null) {
            return List.of();
        }
        if (!permUnit.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        //当前用户拥有的APP权限
        List<ResPermDTO> currentUserPerms = permUnitUserService.listPerms(vo.getAppId());
        if (CollectionUtils.isEmpty(currentUserPerms)) {
            return List.of();
        }
        //已授权的节点
        Set<String> grantIds = baseMapper.selectList(new QueryWrapper<PermUnitResource>()
                        .eq(PermUnitResource.COL_UNIT_ID, vo.getUnitId())
                        .eq(PermUnitResource.COL_APP_ID, vo.getAppId())
                        .select(PermUnitResource.COL_PERM_ID))
                .stream().map(PermUnitResource::getPermId).collect(Collectors.toSet());
        ResourceTreeQueryVO queryVO = new ResourceTreeQueryVO();
        queryVO.setAppId(vo.getAppId());
        queryVO.setWithPerm(true);

        if (vo.getOnlyPacked()) {
            //计算交集
            currentUserPerms.removeIf(perm -> !grantIds.contains(perm.getId()));
            if (currentUserPerms.isEmpty()) {
                return List.of();
            }
        }
        //基于用户权限封装的权限树，移除无权限的节点
        TreeNode<ResTreeDTO> tree = resourceTreeService.listResTreeRecursively(
                queryVO, currentUserPerms, true);
        if (vo.getOnlyPacked()) {
            return tree.getChildren();
        }
        if (!grantIds.isEmpty()) {
            List<String> granted = resourcePermMapper.getPermResIdPaths(grantIds);
            for (String s : granted) {
                grantIds.addAll(Splitter.on(G.ID_PATH_SPLITTER).splitToList(s));
            }
        }
        remarkTree(tree, grantIds);
        return tree.getChildren();
    }

    private void remarkTree(TreeNode<ResTreeDTO> tree, Set<String> grantIds) {
        if (tree.getData().getPerm() != null) {
            tree.getData().setGranted(grantIds.contains(tree.getData().getPerm().getId()));
        } else if (tree.getData().getRes() != null) {
            tree.getData().setGranted(grantIds.contains(tree.getData().getRes().getId()));
        }
        for (TreeNode<ResTreeDTO> child : tree.getChildren()) {
            remarkTree(child, grantIds);
        }
    }

    /**
     * 获取上下文的资源树
     * <p>
     * 不按 {@code hidden} 过滤菜单：隐藏菜单仍参与查询并返回其下权限点，菜单显隐由前端根据 {@code res.hidden} 控制。
     * 若此处强制 {@code hidden=0}，则隐藏父菜单不会进入 listResTree 结果集，导致挂在该菜单上的权限点无法查出。
     *
     * @return 移除了未授权资源
     */
    public List<TreeNode<ResTreeDTO>> listUserResources(ClientResQueryVO vo) {
        OrgTree orgTree = orgTreeMapper.selectById(vo.getOrgId());
        boolean isPrj = orgTree.getNodeCategory().equals(OrgNodeCategoryEnum.PROJECT.getValue());
        List<ResPermDTO> currentUserPerms = permUnitUserService.listPerms(vo.getAppId());
        if (CollectionUtils.isEmpty(currentUserPerms)) {
            return new ArrayList<>();
        }
        ResourceTree parent = null;
        if (StringUtils.isNotBlank(vo.getAppId()) && StringUtils.isNotBlank(vo.getParentCustomId())) {
            parent = resourceTreeMapper.selectOne(new QueryWrapper<ResourceTree>()
                    .eq(ResourceTree.COL_CUSTOM_ID, vo.getParentCustomId())
                    .eq(ResourceTree.COL_APP_ID, vo.getAppId()));
            if (parent == null) {
                return new ArrayList<>();
            }
        }
        ResourceTreeQueryVO queryVO = new ResourceTreeQueryVO();
        queryVO.setAppId(vo.getAppId());
        queryVO.setClientType(vo.getClientType());
        queryVO.setWithPerm(vo.getWithPerm());
        //这里是给前端用的，不需要api
        queryVO.setWithApi(false);
        // 不设置 hidden：包含隐藏菜单，权限点与菜单一并返回；显隐由前端处理
        queryVO.setResType(vo.getResType());
        if (parent != null) {
            queryVO.setParentId(parent.getId());
            queryVO.setParentLevel(vo.getParentLevel());
        }
        queryVO.setShowLevel(isPrj ? ResourceShowLevelEnum.PRJ.getValue() : ResourceShowLevelEnum.ORG.getValue());
        TreeNode<ResTreeDTO> root = resourceTreeService.listResTreeRecursively(
                queryVO, currentUserPerms, true);
        //如果查询的是本下，root节点也要返回
        if (StringUtils.isNotBlank(vo.getParentCustomId()) &&
                TreeQueryLevelEnum.CURRENT_AND_CHILD.getCode().equals(vo.getParentLevel())) {
            return List.of(root);
        }
        return root.getChildren();
    }
}

