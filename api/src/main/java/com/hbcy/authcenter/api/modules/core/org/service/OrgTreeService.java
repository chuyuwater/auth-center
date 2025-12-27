package com.hbcy.authcenter.api.modules.core.org.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Splitter;
import com.hbcy.authcenter.api.common.bean.NameCacheService;
import com.hbcy.authcenter.api.common.bean.NodeMoveVO;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.common.enums.OrgNodeCategoryEnum;
import com.hbcy.authcenter.api.common.enums.OrgNodeTypeEnum;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.vo.OrgTreeCreateVO;
import com.hbcy.authcenter.api.modules.core.org.vo.OrgTreeQueryVO;
import com.hbcy.authcenter.api.modules.core.org.vo.OrgTreeUpdateVO;
import com.hbcy.authcenter.api.modules.core.user.dao.UserOrgMapper;
import com.hbcy.authcenter.api.modules.core.user.model.UserOrg;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 组织架构树相关业务逻辑
 *
 * @author 姚泰然
 * @date 2025-12-25
 */
@Service
public class OrgTreeService extends ServiceImpl<OrgTreeMapper, OrgTree> {
    public static final String BIZ_KEY = "portal:orgtree:tenant:%s:%d";
    public static final String NAME_CACHE_KEY = "portal:orgtree:name:%s";
    /**
     * 节点类型规则
     */
    public static final Map<Integer, Set<Integer>> ALLOW_PARENT_NODE_TYPE = Map.of(
            OrgNodeTypeEnum.ORG.getValue(), Set.of(OrgNodeTypeEnum.ORG.getValue(), OrgNodeTypeEnum.DEPT.getValue()),
            OrgNodeTypeEnum.DEPT.getValue(), Set.of(OrgNodeTypeEnum.DEPT.getValue())
    );
    /**
     * 组织类别节点规则
     */
    public static final Map<Integer, Set<Integer>> ALLOW_CHILD_NODE_CATEGORY = Map.of(
            OrgNodeCategoryEnum.PROJECT.getValue(), Set.of(),
            OrgNodeCategoryEnum.COMPANY.getValue(), Set.of(
                    OrgNodeCategoryEnum.PROJECT.getValue(),
                    OrgNodeCategoryEnum.SUB_COMPANY.getValue(),
                    OrgNodeCategoryEnum.BRANCH_COMPANY.getValue()
            ),
            OrgNodeCategoryEnum.BRANCH_COMPANY.getValue(), Set.of(OrgNodeCategoryEnum.PROJECT.getValue()),
            OrgNodeCategoryEnum.SUB_COMPANY.getValue(), Set.of(
                    OrgNodeCategoryEnum.PROJECT.getValue(),
                    OrgNodeCategoryEnum.SUB_COMPANY.getValue(),
                    OrgNodeCategoryEnum.BRANCH_COMPANY.getValue()
            )
    );
    @Resource
    private NameCacheService nameCacheService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisIdGenerator redisIdGenerator;
    @Resource
    private UserOrgMapper userOrgMapper;

    /**
     * 根据idPath找到部门最近的组织
     *
     * @param idPath 节点全路径
     * @return 组织id
     */
    public static String findDeptDirectOrg(String idPath) {
        //用'-'分割之后，倒序查找含有"ORG"的部分
        List<String> parts = Splitter.on("-").splitToList(idPath);
        for (int i = parts.size() - 1; i >= 0; i--) {
            String part = parts.get(i);
            if (part.contains("ORG")) {
                return part;
            }
        }
        return null;
    }

    private void cleanNameCache(String userId) {
        stringRedisTemplate.opsForHash().delete(NAME_CACHE_KEY, userId);
    }

    private void cleanNameCache(Collection<String> userIds) {
        stringRedisTemplate.opsForHash().delete(NAME_CACHE_KEY, userIds.toArray());
    }

    private OrgTree checkParentId(String tenantId, String parentId) {
        if (StringUtils.isNotBlank(parentId)) {
            OrgTree parent = getById(parentId);
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

    /**
     * 检查是否允许挂载
     *
     * @param node   当前节点
     * @param parent 父节点
     */
    private void checkLevelAllow(OrgTree node, OrgTree parent) {
        if (parent == null || tenantRootId().equals(parent.getId())) {
            //根节点特殊规则
            //根节点下面只能挂组织
            if (!OrgNodeTypeEnum.ORG.getValue().equals(node.getNodeType())) {
                throw new ParamError("根节点下面只能挂组织");
            }
            if (!OrgNodeCategoryEnum.COMPANY.getValue().equals(node.getNodeCategory())) {
                throw new ParamError("根节点下面只能挂公司");
            }
            return;
        }
        if (!ALLOW_PARENT_NODE_TYPE.getOrDefault(parent.getNodeType(), Set.of()).contains(node.getNodeType())) {
            throw new ParamError("父节点类型不允许挂载该节点类型");
        }
        if (!ALLOW_CHILD_NODE_CATEGORY.getOrDefault(parent.getNodeCategory(), Set.of()).contains(node.getNodeCategory())) {
            throw new ParamError("父节点类别不允许挂载该节点类别");
        }
    }

    /**
     * 创建组织节点
     *
     * @param vo 节点信息
     * @return 创建后的节点信息
     */
    @Transactional(rollbackFor = Exception.class)
    public OrgTree create(OrgTreeCreateVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        OrgTree parent = checkParentId(tenantId, vo.getParentId());
        final String parentId = parent == null ? tenantRootId() : parent.getId();
        String parentIdPath = parent == null ? parentId : parent.getIdPath();

        OrgTree entity = new OrgTree();
        BeanCopyUtils.copy(vo, entity);
        checkLevelAllow(entity, parent);
        final boolean isDept = OrgNodeTypeEnum.DEPT.getValue().equals(vo.getNodeType());
        if (!isDept) {
            //同一租户下的组织名称不能重复，但是这里数据库没有唯一键，因此不严谨
            Long n = baseMapper.selectCount(new QueryWrapper<OrgTree>()
                    .eq(OrgTree.COL_NODE_NAME, entity.getNodeName())
                    .eq(OrgTree.COL_TENANT_ID, tenantId)
                    .eq(OrgTree.COL_NODE_TYPE, OrgNodeTypeEnum.ORG.getValue());
            if (n > 0) {
                throw new ParamError("组织名不能重复");
            }
        }
        String id = redisIdGenerator.generateId(BIZ_KEY.formatted(tenantId, vo.getNodeType()), () -> {
            Long count = baseMapper.selectCount(new QueryWrapper<OrgTree>()
                    .eq(OrgTree.COL_TENANT_ID, tenantId)
                    .eq(OrgTree.COL_NODE_TYPE, vo.getNodeType())
            );
            //由于存在虚拟根组织，组织的id是从0开始的
            return isDept ? count + 1 : count;
        }, key -> {
            if (isDept) {
                return OrgTree.ORG_ID_TEMPLATE.formatted(tenantId, key);
            } else {
                return OrgTree.DEPT_ID_TEMPLATE.formatted(tenantId, key);
            }
        });
        entity.setId(id);
        entity.setTenantId(tenantId);
        entity.setCreateUser(UserContextUtils.getUserId());
        entity.setUpdateUser(UserContextUtils.getUserId());
        entity.setIdPath(parentIdPath + G.ID_PATH_SPLITTER + entity.getId());
        try {
            baseMapper.append(entity);
        } catch (DuplicateKeyException e) {
            throw new ParamError("同一层级的名称、简称均不能重复");
        }
        return entity;
    }

    /**
     * 更新组织节点
     *
     * @param vo 更新信息
     * @param id 节点ID
     * @return 更新后的节点信息
     */
    @Transactional(rollbackFor = Exception.class)
    public OrgTree update(OrgTreeUpdateVO vo, String id) {
        OrgTree entity = getById(id);
        if (entity == null) {
            throw new ParamError("指定节点不存在");
        }
        if (!entity.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        String root = tenantRootId();
        if (root.equals(entity.getId())) {
            throw new PermissionError("禁止更新根节点");
        }
        if (!vo.getNodeName().equals(entity.getNodeName())) {
            cleanNameCache(entity.getId());
        }
        OrgTree parent = getById(entity.getParentId());
        BeanCopyUtils.copy(vo, entity);
        checkLevelAllow(entity, parent);

        entity.setUpdateUser(UserContextUtils.getUserId());
        try {
            updateById(entity);
        } catch (DuplicateKeyException e) {
            throw new ParamError("同一层级的名称、简称均不能重复");
        }
        return entity;
    }

    /**
     * 构建组织架构树
     * 完整的组织树可能数据量较大，使用懒加载方式更合适
     *
     * @param vo 查询条件
     * @return 树
     */
    public TreeNode<OrgTree> listOrgTreeRecursively(OrgTreeQueryVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        String parentId = StringUtils.isBlank(vo.getParentId()) ?
                OrgTree.ORG_ID_TEMPLATE.formatted(tenantId, 0) :
                vo.getParentId();
        OrgTree rootData = baseMapper.selectById(parentId);
        if (rootData == null) {
            throw new ParamError("指定节点不存在");
        }
        if (!tenantId.equals(rootData.getTenantId())) {
            throw new PermissionError();
        }
        TreeNode<OrgTree> root = new TreeNode<>(rootData);

        List<OrgTree> orgTrees = baseMapper.listChildrenRecursively(
                tenantId, rootData.getIdPath() + G.ID_PATH_SPLITTER, vo
        );
        //fill user
        Set<String> userIds = orgTrees.stream().map(OrgTree::getCreateUser).collect(Collectors.toSet());
        Map<String, String> userNameMap = nameCacheService.getUserNameMap(userIds);
        orgTrees.forEach(t -> t.setCreateUserName(userNameMap.get(t.getCreateUser())));

        Map<String, List<OrgTree>> childrenMap = orgTrees.stream()
                .collect(Collectors.groupingBy(node ->
                                StringUtils.isBlank(node.getParentId()) ? "" : node.getParentId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(), l -> {
                                    l.sort(Comparator.comparingInt(OrgTree::getShowOrder));
                                    return l;
                                })));

        String rootId = root.getData().getId();
        buildTree(root, childrenMap, rootId);
        return root;
    }

    private void buildTree(TreeNode<OrgTree> current,
                           Map<String, List<OrgTree>> childrenMap,
                           String parentId) {
        List<OrgTree> children = childrenMap.get(parentId);
        if (children != null) {
            for (OrgTree t : children) {
                TreeNode<OrgTree> node = new TreeNode<>();
                node.setData(t);
                current.addChild(node);
                buildTree(node, childrenMap, t.getId());
            }
        }
    }

    public String tenantRootId() {
        return OrgTree.ORG_ID_TEMPLATE.formatted(UserContextUtils.getTenantId(), 0);
    }

    /**
     * 查询组织下级节点
     */
    public List<OrgTree> listDirectChildren(OrgTreeQueryVO vo) {
        if (StringUtils.isBlank(vo.getParentId())) {
            vo.setParentId(tenantRootId());
        } else {
            checkParentId(UserContextUtils.getTenantId(), vo.getParentId());
        }
        return baseMapper.selectList(new QueryWrapper<OrgTree>()
                .eq(OrgTree.COL_PARENT_ID, vo.getParentId())
                .eq(OrgTree.COL_TENANT_ID, UserContextUtils.getTenantId())
                .eq(vo.getNodeType() != null, OrgTree.COL_NODE_TYPE, vo.getNodeType())
                .eq(vo.getNodeCategory() != null, OrgTree.COL_NODE_CATEGORY, vo.getNodeCategory())
                .or(StringUtils.isNotBlank(vo.getName()))
                .like(OrgTree.COL_NODE_NAME, vo.getName())
                .like(OrgTree.COL_SHORT_NAME, vo.getName())
                .orderByAsc(OrgTree.COL_SHOW_ORDER)
        );
    }

    /**
     * 删除组织节点
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        OrgTree node = getById(id);
        if (node == null) {
            return;
        }
        if (!node.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        if (node.getId().equals(tenantRootId())) {
            throw new PermissionError("不能删除根节点");
        }
        boolean isDept = node.getNodeType().equals(OrgNodeTypeEnum.DEPT.getValue());
        Long count = userOrgMapper.selectCount(new QueryWrapper<UserOrg>()
                .eq(UserOrg.COL_TENANT_ID, node.getTenantId())
                .eq(!isDept, UserOrg.COL_ORG_ID, node.getId())
                .eq(isDept, UserOrg.COL_DEPT_ID, node.getId())
        );
        if (count > 0) {
            String tips = isDept ? "部门" : "组织";
            throw new PermissionError("删除%s内存在用户，需要将用户移出才能删除".formatted(tips));
        }
        List<OrgTree> related = baseMapper.listChildrenRecursively(node.getTenantId(), node.getIdPath(), null);
        Set<String> ids = related.stream().map(OrgTree::getId).collect(Collectors.toSet());
        ids.add(id);
        cleanNameCache(ids);
        baseMapper.deleteByIds(ids);
    }

    /**
     * 移动组织节点
     *
     * @param vo 移动详情
     */
    @Transactional(rollbackFor = Exception.class)
    public void move(@Valid NodeMoveVO vo) {
        OrgTree node = getById(vo.getNodeId());
        if (node == null) {
            throw new ParamError("节点不存在");
        }
        if (!node.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        OrgTree parentNode = null;
        OrgTree prevNode = null;
        if (StringUtils.isNotBlank(vo.getParentId())) {
            parentNode = getById(vo.getParentId());
            if (parentNode == null) {
                throw new ParamError("父节点不存在");
            }
            if (!parentNode.getTenantId().equals(node.getTenantId())) {
                throw new ParamError("当前节点和父节点属于不同的租户");
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
        //校验移动的合法性
        checkLevelAllow(node, parentNode);

        // Correct handling of ID Path updates
        String oldPath = node.getIdPath();
        String newPath = node.getId();
        if (parentNode != null) {
            newPath = parentNode.getIdPath() + G.ID_PATH_SPLITTER + node.getId();
        }

        if (!vo.getParentId().equals(node.getParentId())) {
            //父节点移动，部门不能跨组织移动
            if (node.getNodeType().equals(OrgNodeTypeEnum.DEPT.getValue())) {
                String directOrg = findDeptDirectOrg(oldPath);
                String newDirectOrg = findDeptDirectOrg(newPath);
                if (!Objects.equals(directOrg, newDirectOrg)) {
                    throw new PermissionError("部门不能跨组织移动");
                }
            }
            // Update children's paths
            baseMapper.updateIdPath(node.getTenantId(), oldPath, newPath);
        }

        // Check the previous node to determine order
        int targetIdx = 0;
        if (prevNode != null) {
            targetIdx = prevNode.getShowOrder() + 1;
        }
        baseMapper.updateShowOrder(node.getTenantId(), vo.getParentId(), targetIdx);

        // Update target node
        OrgTree toUpdate = new OrgTree();
        toUpdate.setId(vo.getNodeId());
        toUpdate.setParentId(vo.getParentId());
        toUpdate.setShowOrder(targetIdx);
        updateById(toUpdate);
    }
}
