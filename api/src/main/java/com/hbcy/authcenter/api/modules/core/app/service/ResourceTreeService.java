package com.hbcy.authcenter.api.modules.core.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.modules.core.app.dao.AppMapper;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourcePermMapper;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourceTreeMapper;
import com.hbcy.authcenter.api.modules.core.app.dto.ResTreeDTO;
import com.hbcy.authcenter.api.modules.core.app.model.App;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourceTreeCreateVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourceTreeQueryVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourceTreeUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.log.JsonLogUtils;
import com.hbcy.common.base.tree.TreeNode;
import jakarta.annotation.Resource;
import net.logstash.logback.argument.StructuredArguments;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ResourcePermMapper resourcePermMapper;

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

    public ResourceTree create(ResourceTreeCreateVO vo) {
        ResourceTree parent = checkParentId(vo.getAppId(), vo.getParentId());

        ResourceTree entity = new ResourceTree();
        BeanUtils.copyProperties(vo, entity);
        entity.setCreateUser(UserContextUtils.getUserId());
        entity.setUpdateUser(UserContextUtils.getUserId());
        if (parent != null) {
            entity.setIdPath(parent.getIdPath() + "," + entity.getId());
        }
        try {
            save(entity);
        } catch (DuplicateKeyException e) {
            throw new ParamError("同一应用下自定义菜单ID不能重复");
        }
        return entity;
    }

    public ResourceTree update(ResourceTreeUpdateVO vo, String id) {
        ResourceTree entity = getById(id);
        if (entity == null) {
            throw new ParamError("指定节点不存在");
        }
        BeanUtils.copyProperties(vo, entity);
        entity.setUpdateUser(UserContextUtils.getUserId());
        try {
            updateById(entity);
        } catch (DuplicateKeyException e) {
            throw new ParamError("同一应用下自定义菜单ID不能重复");

        }
        return entity;
    }

    /**
     * 构建权限资源树
     *
     * @param vo 查询条件
     * @return 树
     */
    public TreeNode<ResTreeDTO> listResTreeRecursively(ResourceTreeQueryVO vo) {
        TreeNode<ResTreeDTO> root = new TreeNode<>();
        if (StringUtils.isNotBlank(vo.getParentId())) {
            ResourceTree node = baseMapper.selectById(vo.getParentId());
            if (node == null) {
                throw new ParamError("指定节点不存在");
            }
            ResTreeDTO dto = new ResTreeDTO();
            dto.setRes(node);
        } else {
            root.setData(new ResTreeDTO());
        }
        List<ResourceTree> resourceTrees = baseMapper.listChildrenRecursively(vo.getAppId(), vo.getParentId());
        List<ResourcePerm> resourcePerms = new ArrayList<>();
        if (vo.isWithPerm()) {
            List<String> ids = resourceTrees.stream().map(ResourceTree::getId).toList();
            resourcePerms = resourcePermMapper.selectList(new QueryWrapper<ResourcePerm>()
                    .in(ResourcePerm.COL_RES_ID, ids));
        }
        Map<String, List<ResourceTree>> resChildrenMap = resourceTrees.stream()
                .collect(Collectors.groupingBy(ResourceTree::getParentId, Collectors.collectingAndThen(
                        Collectors.toList(), l -> {
                            l.sort(Comparator.comparingInt(ResourceTree::getShowOrder));
                            return l;
                        })));
        Map<String, List<ResourcePerm>> permChildrenMap = resourcePerms.stream()
                .collect(Collectors.groupingBy(ResourcePerm::getResId, Collectors.toList()));
        buildTree(root, resChildrenMap, permChildrenMap);
        return root;
    }

    private void buildTree(TreeNode<ResTreeDTO> current,
                           Map<String, List<ResourceTree>> childrenMap,
                           Map<String, List<ResourcePerm>> permMap) {
        String resId = current.getData().getRes().getId();
        //先查询当前节点关联的权限点
        for (ResourcePerm t : permMap.get(resId)) {
            TreeNode<ResTreeDTO> node = new TreeNode<>();
            node.setData(new ResTreeDTO().setPerm(t));
            current.addChild(node);
        }
        //再递归查询当前节点关联的菜单
        for (ResourceTree t : childrenMap.get(resId)) {
            TreeNode<ResTreeDTO> node = new TreeNode<>();
            node.setData(new ResTreeDTO().setRes(t));
            current.addChild(node);
            buildTree(node, childrenMap, permMap);
        }
    }

    /**
     * 查询权限资源列表
     *
     * @param vo 查询条件
     * @return 列表结果
     */
    public List<ResourceTree> listResTree(ResourceTreeQueryVO vo) {
        return baseMapper.listChildrenRecursively(vo.getAppId(), vo.getParentId());
    }

    /**
     * 删除资源节点
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        //需要删除其下的所有子节点和附属的资源
        ResourceTree node = getById(id);
        if (node == null) {
            return;
        }
        List<ResourceTree> related = baseMapper.listChildrenRecursively(node.getAppId(), node.getId());
        Set<String> resIds = related.stream().map(ResourceTree::getId).collect(Collectors.toSet());
        resIds.add(id);
        Set<String> permIds = resourcePermMapper.selectList(new QueryWrapper<ResourcePerm>()
                .select(ResourcePerm.COL_ID)
                .in(ResourcePerm.COL_RES_ID, resIds)).stream().map(ResourcePerm::getId).collect(Collectors.toSet());
        baseMapper.deleteByIds(resIds);
        resourcePermMapper.deleteByIds(permIds);
        JsonLogUtils.log("delete-res-node", StructuredArguments.kv("id", id),
                StructuredArguments.kv("permIds", permIds),
                StructuredArguments.kv("resIds", resIds));
        //TODO：删除对应功能的授权
    }
}
