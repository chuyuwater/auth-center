package com.hbcy.authcenter.api.modules.core.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.google.common.base.Splitter;
import com.hbcy.authcenter.api.common.bean.NameCacheService;
import com.hbcy.authcenter.api.common.bean.NodeMoveVO;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.modules.core.app.dao.AppMapper;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourceTreeMapper;
import com.hbcy.authcenter.api.modules.core.app.dto.ResPermDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResTreeDTO;
import com.hbcy.authcenter.api.modules.core.app.model.App;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermCreateVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourceTreeCreateVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourceTreeQueryVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourceTreeUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.log.JsonLogUtils;
import com.hbcy.common.base.tree.TreeNode;
import com.hbcy.common.base.util.BeanCopyUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源树（菜单）相关业务逻辑
 *
 * @author 姚泰然
 * @date 2025-12-24
 */
@Service
public class ResourceTreeService extends ServiceImpl<ResourceTreeMapper, ResourceTree> {
    @Resource
    private AppMapper appMapper;
    @Resource
    private ResourcePermService resourcePermService;
    @Resource
    private NameCacheService nameCacheService;

    private ResourceTree checkParentId(String appId, String parentId) {
        App app = appMapper.selectById(appId);
        if (app == null) {
            throw new ParamError("应用不存在");
        }
        if (StringUtils.isNotBlank(parentId)) {
            ResourceTree parent = getById(parentId);
            if (parent == null) {
                throw new ParamError("父资源不存在");
            }
            if (!appId.equals(parent.getAppId())) {
                throw new ParamError("父资源不属于当前应用");
            }
            return parent;
        }
        return null;
    }

    private void checkSubPerms(List<ResourcePermCreateVO> subPerms) {
        if (subPerms == null) {
            return;
        }
        for (ResourcePermCreateVO subPerm : subPerms) {
            String apiPath = subPerm.getApiPath();
            List<String> parts = Splitter.on("/").splitToList(apiPath);
            if (parts.size() < 3) {
                throw new ParamError("API路径过短");
            }
            if (apiPath.contains("**") && !"**".equals(parts.get(parts.size() - 1))) {
                throw new ParamError("API路径中，**只能放在末尾");
            }
        }
    }

    /**
     * 创建资源节点（菜单）
     *
     * @param vo 节点信息
     * @return 创建后的节点信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ResourceTree create(ResourceTreeCreateVO vo) {
        checkSubPerms(vo.getSubPerms());
        ResourceTree parent = checkParentId(vo.getAppId(), vo.getParentId());
        ResourceTree entity = new ResourceTree();
        BeanCopyUtils.copy(vo, entity);
        entity.setId(UlidCreator.getUlid().toString());
        entity.setCreateUser(UserContextUtils.getUserId());
        entity.setUpdateUser(UserContextUtils.getUserId());
        if (parent != null) {
            entity.setIdPath(parent.getIdPath() + G.ID_PATH_SPLITTER + entity.getId());
        } else {
            entity.setIdPath(entity.getId());
        }
        try {
            baseMapper.append(entity);
            resourcePermService.batchCreate(entity.getId(), vo.getSubPerms());
        } catch (DuplicateKeyException e) {
            throw new ParamError("同一应用下菜单唯一ID不能重复");
        }
        return entity;
    }

    /**
     * 更新资源节点（菜单）
     *
     * @param vo 更新信息
     * @param id 节点ID
     * @return 更新后的节点信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ResourceTree update(ResourceTreeUpdateVO vo, String id) {
        checkSubPerms(vo.getSubPerms());
        ResourceTree entity = getById(id);
        if (entity == null) {
            throw new ParamError("指定节点不存在");
        }
        BeanCopyUtils.copy(vo, entity);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateUser(UserContextUtils.getUserId());
        try {
            updateById(entity);
            resourcePermService.overwrite(id, vo.getSubPerms(), entity.getAppId());
        } catch (DuplicateKeyException e) {
            throw new ParamError("同一应用下自定义菜单ID不能重复");
        }
        return entity;
    }

    public TreeNode<ResTreeDTO> listResTreeRecursively(ResourceTreeQueryVO vo) {
        return listResTreeRecursively(vo, null, false);
    }

    /**
     * 构建权限资源树
     * 有以下返回情况：
     * 1. 全量的资源树（菜单、菜单+权限点）
     * 2. 给用户已授权的资源树（移除了未授权的节点，菜单、菜单+权限点）
     * 3. 全量的资源树，但是标记区分了已授权和未授权的部分（菜单、菜单+权限点）
     *
     * @param vo            查询条件
     * @param grantPermList 已授权的权限，如果不为null，则用来过滤
     * @param removeUngrant 是否移除掉未授权的节点，如果不移除则标记为未授权
     * @return 树
     */
    public TreeNode<ResTreeDTO> listResTreeRecursively(ResourceTreeQueryVO vo,
                                                       List<ResPermDTO> grantPermList, boolean removeUngrant) {
        TreeNode<ResTreeDTO> root = new TreeNode<>();
        ResTreeDTO dto = new ResTreeDTO();
        root.setData(dto);
        if (StringUtils.isNotBlank(vo.getParentId())) {
            ResourceTree node = baseMapper.selectById(vo.getParentId());
            if (node == null) {
                throw new ParamError("指定节点不存在");
            }
            dto.setRes(node);
        }
        List<ResourceTree> resourceTrees = listResTree(vo);
        List<ResourcePerm> resourcePerms = new ArrayList<>();
        if (vo.isWithPerm()) {
            List<String> ids = resourceTrees.stream().map(ResourceTree::getId).toList();
            resourcePerms = resourcePermService.list(new QueryWrapper<ResourcePerm>()
                    .in(ResourcePerm.COL_RES_ID, ids));
        }

        Set<String> allGrantIds = filterGranted(grantPermList, removeUngrant, resourceTrees, resourcePerms);
        if (vo.isWithCreator()) {
            //填充创建者信息
            Set<String> creator = new HashSet<>();
            for (ResourceTree resourceTree : resourceTrees) {
                creator.add(resourceTree.getCreateUser());
            }
            for (ResourcePerm resourcePerm : resourcePerms) {
                creator.add(resourcePerm.getCreateUser());
            }
            Map<String, String> userNameMap = nameCacheService.getUserNameMap(creator);
            resourceTrees.forEach(t -> t.setCreateUserName(userNameMap.get(t.getCreateUser())));
            resourcePerms.forEach(t -> t.setCreateUserName(userNameMap.get(t.getCreateUser())));
        }
        Map<String, List<ResourceTree>> resChildrenMap = resourceTrees.stream()
                .collect(Collectors.groupingBy(ResourceTree::getParentId, Collectors.collectingAndThen(
                        Collectors.toList(), l -> {
                            l.sort(Comparator.comparingInt(ResourceTree::getShowOrder));
                            return l;
                        })));
        Map<String, List<ResourcePerm>> permChildrenMap = resourcePerms.stream()
                .collect(Collectors.groupingBy(ResourcePerm::getResId, Collectors.toList()));
        buildTree(root, resChildrenMap, permChildrenMap, allGrantIds);
        return root;
    }

    @Nullable
    private Set<String> filterGranted(List<ResPermDTO> grantPermList, boolean removeUngrant,
                                      List<ResourceTree> resourceTrees, List<ResourcePerm> resourcePerms) {
        if (grantPermList == null) {
            return null;
        }
        //权限点关联的直接资源节点
        Set<String> directIds = new HashSet<>();
        //授权的资源节点
        Set<String> grantResIds = new HashSet<>();
        //授权的权限点
        Set<String> grantPermIds = new HashSet<>();
        for (ResPermDTO dto : grantPermList) {
            directIds.add(dto.getResId());
            grantPermIds.add(dto.getId());
        }
        if (!directIds.isEmpty()) {
            List<ResourceTree> grantRes = baseMapper.selectList(new QueryWrapper<ResourceTree>().
                    in(ResourceTree.COL_ID, directIds)
                    .select(ResourceTree.COL_ID_PATH));
            for (ResourceTree rt : grantRes) {
                grantResIds.addAll(Splitter.on(G.ID_PATH_SPLITTER).splitToList(rt.getIdPath()));
            }
        }
        if (removeUngrant) {
            //在这里直接移除掉未授权的节点
            resourceTrees.removeIf(x -> !grantResIds.contains(x.getId()));
            resourcePerms.removeIf(x -> !grantPermIds.contains(x.getId()));
        } else {
            //下文构建树的时候标记授权
            Set<String> allGrantIds = new HashSet<>();
            allGrantIds.addAll(grantPermIds);
            allGrantIds.addAll(grantResIds);
            return allGrantIds;
        }
        return null;
    }

    private void buildTree(TreeNode<ResTreeDTO> current,
                           Map<String, List<ResourceTree>> childrenMap,
                           Map<String, List<ResourcePerm>> permMap,
                           Set<String> allGrantIds) {
        String resId = Optional.ofNullable(
                        current.getData()).map(ResTreeDTO::getRes)
                .map(ResourceTree::getId).orElse("");
        //先查询当前节点关联的权限点
        for (ResourcePerm t : permMap.getOrDefault(resId, Collections.emptyList())) {
            TreeNode<ResTreeDTO> node = new TreeNode<>();
            ResTreeDTO dto = new ResTreeDTO().setPerm(t);
            if (allGrantIds != null) {
                dto.setGranted(allGrantIds.contains(t.getId()));
            }
            node.setData(dto);
            current.addChild(node);
        }
        //再递归查询当前节点关联的菜单
        for (ResourceTree t : childrenMap.getOrDefault(resId, Collections.emptyList())) {
            TreeNode<ResTreeDTO> node = new TreeNode<>();
            ResTreeDTO dto = new ResTreeDTO().setRes(t);
            if (allGrantIds != null) {
                dto.setGranted(allGrantIds.contains(t.getId()));
            }
            node.setData(dto);
            current.addChild(node);
            buildTree(node, childrenMap, permMap, allGrantIds);
        }
    }

    /**
     * 查询权限资源列表
     *
     * @param vo 查询条件
     * @return 列表结果
     */
    public List<ResourceTree> listResTree(ResourceTreeQueryVO vo) {
        String idPath = "";
        if (StringUtils.isNotBlank(vo.getParentId())) {
            ResourceTree tree = getById(vo.getParentId());
            if (tree == null) {
                throw new ParamError("父节点不存在");
            }
            idPath = tree.getIdPath() + G.ID_PATH_SPLITTER;
        }
        vo.setIdPath(idPath);
        List<ResourceTree> resourceTrees = baseMapper.listChildren(vo);
        Set<String> ids = new HashSet<>();
        for (ResourceTree rt : resourceTrees) {
            ids.addAll(Splitter.on(G.ID_PATH_SPLITTER).splitToList(rt.getIdPath()));
        }
        if (ids.size() > resourceTrees.size()) {
            resourceTrees = baseMapper.selectByIds(ids);
        }
        return resourceTrees;
    }

    /**
     * 删除资源节点
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id, boolean force) {
        //需要删除其下的所有子节点和附属的资源
        ResourceTree node = getById(id);
        if (node == null) {
            return;
        }
        ResourceTreeQueryVO vo = new ResourceTreeQueryVO();
        vo.setAppId(node.getAppId());
        vo.setIdPath(node.getIdPath());
        List<ResourceTree> related = baseMapper.listChildren(vo);
        Set<String> resIds = related.stream().map(ResourceTree::getId).collect(Collectors.toSet());
        resIds.add(id);
        if (!force && resIds.size() > 1) {
            throw new ParamError("请先删除该菜单下的所有子资源");
        }
        Set<String> permIds = resourcePermService.list(new QueryWrapper<ResourcePerm>()
                .select(ResourcePerm.COL_ID)
                .in(ResourcePerm.COL_RES_ID, resIds)).stream().map(
                ResourcePerm::getId).collect(Collectors.toSet());
        if (!force && !permIds.isEmpty()) {
            throw new ParamError("请先删除该菜单下的所有权限点");
        }
        baseMapper.deleteByIds(resIds);
        JsonLogUtils.log("delete-res-node", "id", id, "permIds", permIds, "resIds", resIds);
        if (permIds.isEmpty()) {
            return;
        }
        resourcePermService.delete(node.getAppId(), permIds);
    }

    /**
     * 移动资源节点
     *
     * @param vo 移动详情
     */
    @Transactional(rollbackFor = Exception.class)
    public void move(@Valid NodeMoveVO vo) {
        ResourceTree node = getById(vo.getNodeId());
        if (node == null) {
            throw new ParamError("节点不存在");
        }
        ResourceTree parentNode = null;
        ResourceTree prevNode = null;
        if (StringUtils.isNotBlank(vo.getParentId())) {
            parentNode = getById(vo.getParentId());
            if (parentNode == null) {
                throw new ParamError("父节点不存在");
            }
            if (!parentNode.getAppId().equals(node.getAppId())) {
                throw new ParamError("当前节点和父节点属于不同的应用");
            }
        }
        if (StringUtils.isNotBlank(vo.getPrevId())) {
            prevNode = getById(vo.getPrevId());
            if (prevNode == null) {
                throw new ParamError("前一个节点不存在");
            }
            if (!prevNode.getParentId().equals(vo.getParentId())) {
                throw new ParamError("前一个节点和当前节点不属于同一个父节点");
            }
        }
        if (!vo.getParentId().equals(node.getParentId())) {
            //父节点被移动，意味着当前节点及其下级节点的id_path需要更新
            String oldPath = node.getIdPath();
            String newPath = parentNode == null ? node.getId() : parentNode.getIdPath()
                    + G.ID_PATH_SPLITTER + node.getId();
            baseMapper.updateIdPath(node.getAppId(), oldPath, newPath);
        }
        //检查前节点，修改被移动节点及其之后节点的show_order
        int targetIdx = 0;
        if (prevNode != null) {
            targetIdx = prevNode.getShowOrder() + 1;
        }
        baseMapper.updateShowOrder(node.getAppId(), vo.getParentId(), targetIdx);

        //最后，修改目标节点自身数据
        ResourceTree toUpdate = new ResourceTree();
        toUpdate.setId(vo.getNodeId());
        toUpdate.setParentId(vo.getParentId());
        toUpdate.setShowOrder(targetIdx);
        updateById(toUpdate);
    }
}
