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
import com.hbcy.authcenter.api.modules.minor.user.vo.OrderedMenuQueryVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.tree.TreeNode;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
    @Autowired
    private ResourceTreeMapper resourceTreeMapper;

    /**
     * 获取当前用户有权访问的一级菜单、二级菜单（移动端）
     * 缓存1分钟，避免频繁查询数据库
     *
     * @param clientType 1:PC端 2:移动端
     * @return 菜单树（1或2级）
     */
    @Cacheable(value = "@1m")
    public List<TreeNode<ResourceTree>> listEntry(String userId, String orgId, int clientType) {
        // 叶子节点资源集合
        Set<String> resIds = new HashSet<>();
        // 全权app
        Set<String> appIds = new HashSet<>();
        //一级菜单和二级菜单节点
        Set<String> filteredResIds = new HashSet<>();

        String tenantId = UserContextUtils.getTenantId();
        OrgTree org = orgTreeMapper.selectById(orgId);
        if (org == null) {
            return List.of();
        }
        boolean isPrj = org.getNodeCategory().equals(OrgNodeCategoryEnum.PROJECT.getValue());
        if (UserContextUtils.isTenantAdmin()) {
            //管理员特殊逻辑
            List<TenantApp> tenantApps = tenantAppMapper.selectList(new QueryWrapper<TenantApp>()
                    .eq(TenantApp.COL_TENANT_ID, tenantId));
            for (TenantApp tenantApp : tenantApps) {
                if (tenantApp.getGrantAll().equals(1)) {
                    appIds.add(tenantApp.getAppId());
                }
            }
            resIds.addAll(tenantAppResourceMapper.getGrantedResIds(tenantId, null));
        } else {
            //普通用户
            resIds.addAll(permUnitUserMapper.listUserRes(userId, org.getIdPath()));
        }
        if (resIds.isEmpty() && appIds.isEmpty()) {
            return List.of();
        }
        //获取满足条件的idPath
        Set<String> idPaths = resourceTreeMapper.listIdPath(resIds, appIds);
        //通过idPath剥离出一级菜单和二级菜单
        for (String idPath : idPaths) {
            String[] split = idPath.split(G.ID_PATH_SPLITTER);
            if (split.length == 1) {
                filteredResIds.add(split[0]);
            } else if (clientType == ClientTypeEnum.MOBILE && split.length >= 2) {
                filteredResIds.add(split[1]);
            }
        }
        List<Integer> showLevels = Lists.newArrayList(ResourceShowLevelEnum.GLOBAL.getValue());
        List<Integer> clientTypes = Lists.newArrayList(ClientTypeEnum.ALL, clientType);
        if (isPrj) {
            showLevels.add(ResourceShowLevelEnum.PRJ.getValue());
        } else {
            showLevels.add(ResourceShowLevelEnum.ORG.getValue());
        }
        //满足条件的菜单
        OrderedMenuQueryVO vo = new OrderedMenuQueryVO();
        vo.setClientTypes(clientTypes);
        vo.setShowLevels(showLevels);
        vo.setResIds(filteredResIds);
        //应用之间的一级菜单按appId的show_order排序
        //应用之内的一级菜单、二级菜单按resId的show_order排序
        List<ResourceTree> nodes = resourceTreeMapper.listOrderdMenu(vo);
        Map<String, List<ResourceTree>> children = new HashMap<>();
        //虚拟根节点
        TreeNode<ResourceTree> root = new TreeNode<>(new ResourceTree());
        for (ResourceTree node : nodes) {
            if (StringUtils.isBlank(node.getParentId())) {
                root.addChild(new TreeNode<>(node));
            } else {
                children.computeIfAbsent(node.getParentId(), k -> new ArrayList<>()).add(node);
            }
        }
        if (!children.isEmpty()) {
            for (TreeNode<ResourceTree> child : root.getChildren()) {
                List<ResourceTree> lv2 = children.get(child.getData().getId());
                if (!CollectionUtils.isEmpty(lv2)) {
                    for (ResourceTree rt : lv2) {
                        child.addChild(new TreeNode<>(rt));
                    }
                }
            }
        }
        return root.getChildren();
    }
}
