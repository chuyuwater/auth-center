package com.hbcy.authcenter.api.modules.core.org.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Splitter;
import com.hbcy.authcenter.api.common.bean.NameCacheService;
import com.hbcy.authcenter.api.common.bean.NodeMoveVO;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.common.enums.OrgNodeCategoryEnum;
import com.hbcy.authcenter.api.common.enums.OrgNodeTypeEnum;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.vo.OrgSwitchStatusVO;
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
import com.hbcy.common.web.api.NamedId;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 组织架构树相关业务逻辑
 *
 * @author 姚泰然
 * @date 2025-12-25
 */
@Slf4j
@Service
public class OrgTreeService extends ServiceImpl<OrgTreeMapper, OrgTree> {
    public static final String BIZ_KEY = "portal:orgtree:tenant:%s:%d:";
    public static final String NAME_CACHE_KEY = "portal:orgtree:name:%s";
    /**
     * 节点类型规则，key：父节点，value：允许的子节点类型
     */
    public static final Map<Integer, Set<Integer>> ALLOW_CHILD_NODE_TYPE = Map.of(
            OrgNodeTypeEnum.ORG.getValue(), Set.of(OrgNodeTypeEnum.ORG.getValue(), OrgNodeTypeEnum.DEPT.getValue()),
            OrgNodeTypeEnum.DEPT.getValue(), Set.of(OrgNodeTypeEnum.DEPT.getValue())
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
        List<String> parts = Splitter.on(G.ID_PATH_SPLITTER).splitToList(idPath);
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
            return;
        }
        if (!ALLOW_CHILD_NODE_TYPE.getOrDefault(parent.getNodeType(), Set.of()).contains(node.getNodeType())) {
            throw new ParamError("父节点类型不允许挂载该节点类型");
        }
        //项目部下面不能挂其他组织
        if (parent.getNodeCategory().equals(OrgNodeCategoryEnum.PROJECT.getValue()) &&
                !node.getNodeType().equals(OrgNodeTypeEnum.DEPT.getValue())) {
            throw new ParamError("项目部下面不能挂其他组织");
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
                    .eq(OrgTree.COL_NODE_TYPE, OrgNodeTypeEnum.ORG.getValue()));
            if (n > 0) {
                throw new ParamError("组织名不能重复");
            }
        }
        String id = generateId(vo.getNodeType(), tenantId);
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

    public String generateId(Integer nodeType, String tenantId) {
        final boolean isDept = OrgNodeTypeEnum.DEPT.getValue().equals(nodeType);
        return redisIdGenerator.generateId(BIZ_KEY.formatted(tenantId, nodeType), () -> {
            Long count = baseMapper.selectCount(new QueryWrapper<OrgTree>()
                    .eq(OrgTree.COL_TENANT_ID, tenantId)
                    .eq(OrgTree.COL_NODE_TYPE, nodeType)
            );
            //由于存在虚拟根组织，组织的id是从0开始的
            return isDept ? count : count - 1;
        }, key -> {
            if (isDept) {
                return OrgTree.DEPT_ID_TEMPLATE.formatted(tenantId, key);
            } else {
                return OrgTree.ORG_ID_TEMPLATE.formatted(tenantId, key);
            }
        });
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
        String rootId = tenantRootId();
        if (rootId.equals(entity.getId())) {
            throw new PermissionError("禁止更新根节点");
        }
        if (!vo.getNodeName().equals(entity.getNodeName())) {
            cleanNameCache(entity.getId());
        }
        if (StringUtils.isBlank(vo.getParentId())) {
            vo.setParentId(rootId);
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
            throw new ParamError("同一层级的名称、简称均不能重复");
        }
        return entity;
    }

    /**
     * 列表形式的组织节点
     *
     * @param vo 查询条件
     * @return 满足条件的列表
     */
    public List<OrgTree> listOrgTree(OrgTreeQueryVO vo, boolean buildTree) {
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

        List<OrgTree> orgTrees = baseMapper.listChildren(
                tenantId, rootData.getIdPath() + G.ID_PATH_SPLITTER, vo
        );
        if (buildTree) {
            //需要向上查询整个路径
            Set<String> ids = new HashSet<>();
            for (OrgTree orgTree : orgTrees) {
                ids.addAll(Splitter.on(G.ID_PATH_SPLITTER).splitToList(orgTree.getIdPath()));
            }
            ids.remove(tenantRootId());
            if (ids.isEmpty()) {
                orgTrees = new ArrayList<>();
            } else {
                orgTrees = baseMapper.selectByIds(ids);
            }
        }
        //fill user
        Set<String> userIds = orgTrees.stream().map(OrgTree::getCreateUser).collect(Collectors.toSet());
        Map<String, String> userNameMap = nameCacheService.getUserNameMap(userIds);
        orgTrees.forEach(t -> t.setCreateUserName(userNameMap.get(t.getCreateUser())));
        return orgTrees;
    }

    /**
     * 构建组织架构树
     * 完整的组织树可能数据量较大，使用懒加载方式更合适
     *
     * @param vo 查询条件
     * @return 树
     */
    public TreeNode<OrgTree> listOrgTreeRecursively(OrgTreeQueryVO vo) {
        String parentId = StringUtils.isBlank(vo.getParentId()) ?
                tenantRootId() : vo.getParentId();
        OrgTree rootData = baseMapper.selectById(parentId);
        TreeNode<OrgTree> root = new TreeNode<>(rootData);
        var orgTrees = listOrgTree(vo, true);
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
                .eq(vo.getForbidden() != null, OrgTree.COL_FORBIDDEN, vo.getForbidden())
                .and(StringUtils.isNotBlank(vo.getKeyword()),
                        qw -> qw.like(OrgTree.COL_NODE_NAME, vo.getKeyword()).or()
                                .like(OrgTree.COL_SHORT_NAME, vo.getKeyword()))
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
        Long count = userOrgMapper.selectCount(new QueryWrapper<UserOrg>()
                .eq(UserOrg.COL_TENANT_ID, node.getTenantId())
                .eq(UserOrg.COL_NODE_ID, node.getId())
        );
        if (count > 0) {
            throw new PermissionError("删除节点内存在用户，需要将用户移出才能删除");
        }
        List<OrgTree> related = baseMapper.listChildren(node.getTenantId(), node.getIdPath(), null);
        Set<String> ids = related.stream().map(OrgTree::getId).collect(Collectors.toSet());
        ids.add(id);
        cleanNameCache(ids);
        //逻辑删除
        baseMapper.update(new UpdateWrapper<OrgTree>()
                .in(OrgTree.COL_ID, ids)
                .set(OrgTree.COL_DELETE_TIME, System.currentTimeMillis())
                .set(OrgTree.COL_UPDATE_USER, UserContextUtils.getUserId()));
    }

    //组织树的父节点一定不为空，至少是虚拟节点
    private void updateParent(OrgTree node, String newParentId) {
        OrgTree parentNode = getById(newParentId);
        if (parentNode == null) {
            throw new ParamError("父节点不存在");
        }
        if (!parentNode.getTenantId().equals(node.getTenantId())) {
            throw new ParamError("当前节点和父节点属于不同的租户");
        }
        //校验移动的合法性
        checkLevelAllow(node, parentNode);
        // Correct handling of ID Path updates
        String oldPath = node.getIdPath();
        String newPath = parentNode.getIdPath() + G.ID_PATH_SPLITTER + node.getId();
        //部门不能跨组织移动
        if (node.getNodeType().equals(OrgNodeTypeEnum.DEPT.getValue())) {
            String directOrg = findDeptDirectOrg(oldPath);
            String newDirectOrg = findDeptDirectOrg(newPath);
            if (!Objects.equals(directOrg, newDirectOrg)) {
                throw new PermissionError("部门不能跨组织移动");
            }
        }
        //确认无同名节点
        if (exists(new QueryWrapper<OrgTree>()
                .eq(OrgTree.COL_PARENT_ID, newParentId)
                .eq(OrgTree.COL_TENANT_ID, node.getTenantId())
                .and(qw -> qw.eq(OrgTree.COL_NODE_NAME, node.getNodeName())
                        .or().eq(OrgTree.COL_SHORT_NAME, node.getShortName()))
        )) {
            throw new ParamError("同一层级的名称、简称均不能重复");
        }
        // Update children's paths
        baseMapper.updateIdPath(node.getTenantId(), oldPath, newPath);
        log.info("update org path {}->{} for tenant {}", oldPath, newPath, node.getTenantId());
    }

    /**
     * 移动组织节点
     *
     * @param vo 移动详情
     */
    @Transactional(rollbackFor = Exception.class)
    public void move(@Valid NodeMoveVO vo) {
        vo.check();
        String rootId = tenantRootId();
        if (vo.getNodeId().equals(rootId)) {
            throw new ParamError("禁止移动根节点");
        }
        OrgTree node = getById(vo.getNodeId());
        if (node == null) {
            throw new ParamError("节点不存在");
        }
        if (!node.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        OrgTree prevNode = null;
        if (StringUtils.isNotBlank(vo.getPrevId())) {
            prevNode = getById(vo.getPrevId());
            if (prevNode == null) {
                throw new ParamError("前一个节点不存在");
            }
            if (!prevNode.getParentId().equals(vo.getParentId())) {
                throw new ParamError("前一个节点和当前节点不属于同一个父节点");
            }
        }
        if (StringUtils.isBlank(vo.getParentId())) {
            vo.setParentId(rootId);
        }
        if (!vo.getParentId().equals(node.getParentId())) {
            updateParent(node, vo.getParentId());
        }
        // Check the previous node to determine order
        int targetIdx = 0;
        if (prevNode != null) {
            targetIdx = prevNode.getShowOrder() + 1;
        }
        baseMapper.updateShowOrder(node.getTenantId(), vo.getParentId(), targetIdx);
        OrgTree toUpdate = new OrgTree();
        toUpdate.setId(vo.getNodeId());
        toUpdate.setParentId(vo.getParentId());
        toUpdate.setShowOrder(targetIdx);
        try {
            updateById(toUpdate);
        } catch (DuplicateKeyException e) {
            throw new ParamError("同一层级的名称、简称均不能重复");
        }
    }

    public Map<String, String> getOrgNameMap(Set<String> orgIds, boolean useFullName) {
        List<NamedId> namedIds = baseMapper.selectNameByIds(orgIds, useFullName);
        return namedIds.stream().collect(Collectors.toMap(NamedId::getItemId, NamedId::getItemName));
    }

    //禁用组织对权限不造成影响
    @Transactional(rollbackFor = Exception.class)
    public void switchStatus(OrgSwitchStatusVO vo) {
        String rootId = tenantRootId();
        if (vo.getNodeId().equals(rootId)) {
            throw new ParamError("禁止操作根节点");
        }
        OrgTree node = baseMapper.selectById(vo.getNodeId());
        if (node == null) {
            throw new ParamError("节点不存在");
        }
        if (!node.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        if (node.getForbidden().equals(vo.getForbidden())) {
            return;
        }
        if (node.getParentId().equals(rootId)) {
            throw new ParamError("无法禁用根组织");
        }
        baseMapper.update(new UpdateWrapper<OrgTree>()
                .likeRight(OrgTree.COL_ID_PATH, node.getIdPath())
                .set(OrgTree.COL_FORBIDDEN, vo.getForbidden()));
    }
}
