package com.hbcy.authcenter.api.modules.minor.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.common.enums.ClientTypeEnum;
import com.hbcy.authcenter.api.common.enums.OrgNodeCategoryEnum;
import com.hbcy.authcenter.api.common.enums.ResourceShowLevelEnum;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourceTreeMapper;
import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitUserMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppResourceMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import com.hbcy.authcenter.api.modules.core.user.dao.UserOrgMapper;
import com.hbcy.authcenter.api.modules.core.user.dto.UserOrgDTO;
import com.hbcy.authcenter.api.modules.minor.user.vo.OrderedMenuQueryVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.tree.TreeNode;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author 姚泰然
 * @date 2026-01-12 13:57
 */
@Service
public class ClientRenderService {
    @Resource
    private TenantAppMapper tenantAppMapper;
    @Resource
    private TenantAppResourceMapper tenantAppResourceMapper;
    @Resource
    private PermUnitUserMapper permUnitUserMapper;
    @Resource
    private OrgTreeMapper orgTreeMapper;
    @Resource
    private ResourceTreeMapper resourceTreeMapper;
    @Resource
    private UserOrgMapper userOrgMapper;

    //过滤用户有权限访问的菜单id
    public Set<String> grantResIds(String userId, OrgTree org, int maxDepth) {
        // 叶子节点资源集合
        Set<String> resIds = new HashSet<>();
        // 全权app
        Set<String> appIds = new HashSet<>();
        //过滤后的节点
        Set<String> filteredResIds = new HashSet<>();

        String tenantId = UserContextUtils.getTenantId();
        if (UserContextUtils.isTenantAdmin()) {
            //管理员特殊逻辑
            List<TenantApp> tenantApps = tenantAppMapper.selectList(new QueryWrapper<TenantApp>()
                    .eq(TenantApp.COL_TENANT_ID, tenantId)
                    .eq(TenantApp.COL_FORBIDDEN, 0)
                    .eq(TenantApp.COL_GRANT_ALL, 1));
            for (TenantApp tenantApp : tenantApps) {
                appIds.add(tenantApp.getAppId());
            }
            resIds.addAll(tenantAppResourceMapper.getGrantedResIds(tenantId, null));
        } else {
            //普通用户
            resIds.addAll(permUnitUserMapper.listUserRes(userId, org.getIdPath()));
        }
        if (resIds.isEmpty() && appIds.isEmpty()) {
            return Set.of();
        }
        //获取满足条件的idPath
        Set<String> idPaths = resourceTreeMapper.listIdPath(resIds, appIds);
        //通过idPath按菜单深度剥离菜单
        for (String idPath : idPaths) {
            String[] split = idPath.split(G.ID_PATH_SPLITTER);
            if (maxDepth > 0) {
                for (int i = 0; i < maxDepth && i < split.length; i++) {
                    filteredResIds.add(split[i]);
                }
            } else {
                filteredResIds.addAll(Arrays.asList(split));
            }
        }
        return filteredResIds;
    }

    /**
     * 获取当前用户有权访问的菜单树
     * 缓存1分钟，避免频繁查询数据库
     *
     * @param maxDepth 菜单最大层级，0标识无限制
     * @return 菜单树
     */
    public List<TreeNode<ResourceTree>> listUserMenu(String userId, String orgId, int clientType, int maxDepth) {
        List<ResourceTree> nodes = listUserGrantedMenus(userId, orgId, clientType, maxDepth);
        if (CollectionUtils.isEmpty(nodes)) {
            return List.of();
        }
        //虚拟根节点
        TreeNode<ResourceTree> root = new TreeNode<>(new ResourceTree());
        Map<String, List<ResourceTree>> children = new HashMap<>();
        for (ResourceTree node : nodes) {
            if (StringUtils.isBlank(node.getParentId())) {
                //一级菜单
                root.addChild(new TreeNode<>(node));
            } else {
                //其他菜单
                children.computeIfAbsent(node.getParentId(), k -> new ArrayList<>()).add(node);
            }
        }
        for (TreeNode<ResourceTree> node : root.getChildren()) {
            buildResTree(node, children);
        }
        return root.getChildren();
    }

    public Set<String> listUserGrantedAppIds(String userId, String orgId, int clientType) {
        List<ResourceTree> nodes = listUserGrantedMenus(userId, orgId, clientType, 0);
        if (CollectionUtils.isEmpty(nodes)) {
            return Set.of();
        }
        Set<String> appIds = new HashSet<>();
        for (ResourceTree node : nodes) {
            if (StringUtils.isNotBlank(node.getAppId())) {
                appIds.add(node.getAppId());
            }
        }
        return appIds;
    }

    private List<ResourceTree> listUserGrantedMenus(String userId, String orgId, int clientType, int maxDepth) {
        OrgTree org = orgTreeMapper.selectById(orgId);
        if (org == null) {
            return List.of();
        }
        Set<String> filteredResIds = grantResIds(userId, org, maxDepth);
        if (CollectionUtils.isEmpty(filteredResIds)) {
            return List.of();
        }
        boolean isPrj = org.getNodeCategory().equals(OrgNodeCategoryEnum.PROJECT.getValue());
        List<Integer> showLevels = Lists.newArrayList(ResourceShowLevelEnum.GLOBAL.getValue());
        List<Integer> clientTypes = Lists.newArrayList(ClientTypeEnum.ALL, clientType);
        if (isPrj) {
            showLevels.add(ResourceShowLevelEnum.PRJ.getValue());
        } else {
            showLevels.add(ResourceShowLevelEnum.ORG.getValue());
        }
        //满足条件的菜单
        OrderedMenuQueryVO vo = new OrderedMenuQueryVO();
        vo.setResTypes(List.of(ResourceTree.RES_TYPE_MENU, ResourceTree.RES_TYPE_LINK));
        vo.setClientTypes(clientTypes);
        vo.setShowLevels(showLevels);
        vo.setResIds(filteredResIds);
        return resourceTreeMapper.listOrderdMenu(vo);
    }

    private void buildResTree(TreeNode<ResourceTree> node, Map<String, List<ResourceTree>> children) {
        if (children.containsKey(node.getData().getId())) {
            for (ResourceTree child : children.get(node.getData().getId())) {
                TreeNode<ResourceTree> sub = new TreeNode<>(child);
                node.addChild(sub);
                buildResTree(sub, children);
            }
        }
    }

    public List<TreeNode<UserOrgDTO>> listUserOrgTree() {
        List<UserOrgDTO> userOrgs = userOrgMapper.listUserOrgs(
                List.of(UserContextUtils.getUserId()), false, 0);
        if (CollectionUtils.isEmpty(userOrgs)) {
            return List.of();
        }
        if (userOrgs.size() == 1) {
            return List.of(new TreeNode<>(userOrgs.get(0)));
        }
        //将列表转成树状结构
        return buildTree(userOrgs);
    }

    public List<TreeNode<UserOrgDTO>> buildTree(List<UserOrgDTO> nodes) {
        if (nodes == null || nodes.isEmpty()) return new ArrayList<>();

        // 1. 建立路径与 Tree 节点的映射
        Map<String, TreeNode<UserOrgDTO>> treeMap = new HashMap<>();
        for (UserOrgDTO node : nodes) {
            treeMap.put(node.getIdPath(), new TreeNode<>(node));
        }
        List<TreeNode<UserOrgDTO>> roots = new ArrayList<>();
        // 2. 再次遍历，组装父子关系
        for (UserOrgDTO node : nodes) {
            String currentPath = node.getIdPath();
            TreeNode<UserOrgDTO> currentTree = treeMap.get(currentPath);
            String parentPath = getClosestAncestorPath(currentPath, treeMap);

            if (parentPath != null) {
                treeMap.get(parentPath).addChild(currentTree);
            } else {
                roots.add(currentTree);
            }
        }
        return roots;
    }

    /**
     * 核心逻辑：向上回溯寻找最近的祖先路径
     */
    private String getClosestAncestorPath(String path, Map<String, TreeNode<UserOrgDTO>> treeMap) {
        String tempPath = path;
        while (tempPath.contains("/")) {
            // 截掉最后一级，例如 "a/b/c" -> "a/b"
            tempPath = tempPath.substring(0, tempPath.lastIndexOf("/"));
            // 如果截取后的路径在 Map 中存在，说明找到了最近的“活”祖先
            if (treeMap.containsKey(tempPath)) {
                return tempPath;
            }
            // 如果不存在，继续 While 循环往上找，直到找不到 "/"
        }
        return null;
    }
}
