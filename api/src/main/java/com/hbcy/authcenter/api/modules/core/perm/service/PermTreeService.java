package com.hbcy.authcenter.api.modules.core.perm.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Splitter;
import com.hbcy.authcenter.api.common.bean.NodeMoveVO;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermTreeMapper;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermTree;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnit;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermTreeCreateVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermTreeQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermTreeUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.tree.TreeNode;
import com.hbcy.common.base.util.BeanCopyUtils;
import com.hbcy.common.redis.RedisIdGenerator;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermTreeService extends ServiceImpl<PermTreeMapper, PermTree> {
    public static final String BIZ_KEY = "portal:perm:group:%s:";
    @Resource
    private PermUnitMapper permUnitMapper;
    @Resource
    private RedisIdGenerator redisIdGenerator;

    private PermTree checkParentId(String tenantId, String parentId) {
        if (StringUtils.isNotBlank(parentId)) {
            PermTree parent = getById(parentId);
            if (parent == null) {
                throw new ParamError("父节点不存在");
            }
            if (!tenantId.equals(parent.getTenantId())) {
                throw new ParamError("父节点不属于当前租户");
            }
            return parent;
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public PermTree create(PermTreeCreateVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        PermTree parent = checkParentId(tenantId, vo.getParentId());
        final String parentId = parent == null ? "" : parent.getId();
        String parentIdPath = parent == null ? "" : parent.getIdPath();

        PermTree entity = new PermTree();
        BeanCopyUtils.copy(vo, entity);
        String id = redisIdGenerator.generateId(BIZ_KEY.formatted(tenantId),
                () -> baseMapper.selectCount(new QueryWrapper<PermTree>()
                        .eq(PermTree.COL_TENANT_ID, tenantId)),
                k -> vo.getPolicyModel() == 0 ?
                        PermTree.RBAC_ID_TEMPLATE.formatted(tenantId, k) :
                        PermTree.ABAC_ID_TEMPLATE.formatted(tenantId, k));
        entity.setId(id);
        entity.setParentId(parentId);
        entity.setTenantId(tenantId);
        entity.setCreateUser(UserContextUtils.getUserId());
        entity.setUpdateUser(UserContextUtils.getUserId());
        entity.setIdPath(parent == null ? entity.getId() : parentIdPath + G.ID_PATH_SPLITTER + entity.getId());
        try {
            baseMapper.append(entity);
        } catch (DuplicateKeyException e) {
            throw new ParamError("同一层级的名称不能重复");
        }
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public PermTree update(PermTreeUpdateVO vo, String id) {
        PermTree entity = getById(id);
        if (entity == null) {
            throw new ParamError("指定节点不存在");
        }
        if (!entity.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        if (!entity.getParentId().equals(vo.getParentId())) {
            if (vo.getParentId().equals(id)) {
                throw new ParamError("父节点不能是自身");
            }
            updateParent(entity, vo.getParentId());
            entity.setShowOrder(baseMapper.getChildMaxShowOrder(entity.getTenantId(), vo.getParentId()) + 1);
        }
        BeanCopyUtils.copy(vo, entity);
        entity.setUpdateUser(UserContextUtils.getUserId());
        entity.setUpdateTime(LocalDateTime.now());
        try {
            updateById(entity);
        } catch (DuplicateKeyException e) {
            throw new ParamError("同一层级的名称不能重复");
        }
        return entity;
    }

    public TreeNode<PermTree> listPermTreeRecursively(PermTreeQueryVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        String parentId = vo.getParentId();
        String pathPrefix;
        PermTree rootData = checkParentId(tenantId, parentId);
        if (rootData == null) {
            rootData = new PermTree();
            rootData.setId(parentId);
            rootData.setIdPath("");
            rootData.setParentId("");
            pathPrefix = "";
        } else {
            pathPrefix = rootData.getIdPath() + G.ID_PATH_SPLITTER;
        }
        TreeNode<PermTree> root = new TreeNode<>(rootData);
        List<PermTree> permTrees = baseMapper.listChildren(tenantId, pathPrefix, vo);
        Set<String> ids = new HashSet<>();
        for (PermTree permTree : permTrees) {
            ids.addAll(Splitter.on(G.ID_PATH_SPLITTER).splitToList(permTree.getIdPath()));
        }
        if (ids.size() > permTrees.size()) {
            permTrees = baseMapper.selectByIds(ids);
        }
        Map<String, List<PermTree>> childrenMap = permTrees.stream()
                .collect(Collectors.groupingBy(PermTree::getParentId,
                        Collectors.collectingAndThen(
                                Collectors.toList(), l -> {
                                    l.sort(Comparator.comparingInt(PermTree::getShowOrder));
                                    return l;
                                })));

        String rootId = root.getData().getId();
        buildTree(root, childrenMap, rootId);
        return root;
    }

    private void buildTree(TreeNode<PermTree> current,
                           Map<String, List<PermTree>> childrenMap,
                           String parentId) {
        List<PermTree> children = childrenMap.get(parentId);
        if (children != null) {
            for (PermTree t : children) {
                TreeNode<PermTree> node = new TreeNode<>();
                node.setData(t);
                current.addChild(node);
                buildTree(node, childrenMap, t.getId());
            }
        }
    }

    public List<PermTree> listDirectChildren(PermTreeQueryVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        if (StringUtils.isNotBlank(vo.getParentId())) {
            checkParentId(tenantId, vo.getParentId());
        }
        return baseMapper.selectList(new QueryWrapper<PermTree>()
                .eq(PermTree.COL_PARENT_ID, vo.getParentId())
                .eq(PermTree.COL_TENANT_ID, tenantId)
                .like(StringUtils.isNotBlank(vo.getKeyword()), PermTree.COL_NODE_NAME, vo.getKeyword())
                .orderByAsc(PermTree.COL_SHOW_ORDER)
        );
    }

    public void delete(String id) {
        PermTree node = getById(id);
        if (node == null) {
            return;
        }
        if (!node.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        List<PermTree> related = baseMapper.listChildren(
                node.getTenantId(), node.getIdPath(), null);
        Set<String> ids = related.stream().map(PermTree::getId).collect(Collectors.toSet());
        ids.add(id);
        boolean any = permUnitMapper.exists(new QueryWrapper<PermUnit>()
                .in(PermUnit.COL_BELONG_TO, ids));
        if (any) {
            throw new ParamError("必须先删除分组及其子分组内的角色");
        }
        baseMapper.update(new UpdateWrapper<PermTree>()
                .in(PermTree.COL_ID, ids)
                .set(PermTree.COL_DELETE_TIME, System.currentTimeMillis())
                .set(PermTree.COL_UPDATE_USER, UserContextUtils.getUserId()));
    }

    private void updateParent(PermTree node, String newParentId) {
        PermTree parentNode = checkParentId(node.getTenantId(), newParentId);
        String oldPath = node.getIdPath();
        String newPath = (parentNode != null ? parentNode.getIdPath() + G.ID_PATH_SPLITTER : "") + node.getId();
        baseMapper.updateIdPath(node.getTenantId(), oldPath, newPath);
    }

    @Transactional(rollbackFor = Exception.class)
    public void move(@Valid NodeMoveVO vo) {
        vo.check();
        PermTree node = getById(vo.getNodeId());
        if (node == null) {
            throw new ParamError("节点不存在");
        }
        if (!node.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        PermTree prevNode = null;
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
            updateParent(node, vo.getParentId());
        }
        int targetIdx = 0;
        if (prevNode != null) {
            targetIdx = prevNode.getShowOrder() + 1;
        }
        baseMapper.updateShowOrder(node.getTenantId(), vo.getParentId(), targetIdx);

        PermTree toUpdate = new PermTree();
        toUpdate.setId(vo.getNodeId());
        toUpdate.setParentId(vo.getParentId());
        toUpdate.setShowOrder(targetIdx);
        updateById(toUpdate);
    }
}
