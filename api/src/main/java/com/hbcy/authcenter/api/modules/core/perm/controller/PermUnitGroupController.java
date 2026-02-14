package com.hbcy.authcenter.api.modules.core.perm.controller;

import com.hbcy.authcenter.api.common.bean.NodeMoveVO;
import com.hbcy.authcenter.api.modules.core.perm.dto.PermGroupDetailDTO;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitGroup;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitGroupService;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitGroupCreateVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitGroupQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitGroupUpdateVO;
import com.hbcy.common.base.tree.TreeNode;
import com.hbcy.common.web.bean.NameFill;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色分组（租户侧）
 *
 * @author 姚泰然
 * @module perm
 * @date 2025-12-28
 */
@RestController
@RequestMapping("api/portal/v1/perm/group")
@Validated
public class PermUnitGroupController {

    @Resource
    private PermUnitGroupService permUnitGroupService;

    /**
     * 节点详情
     *
     * @param id 节点ID
     * @return 节点信息
     */
    @GetMapping("/node/{id}")
    @NameFill
    public PermGroupDetailDTO getById(@PathVariable String id) {
        return permUnitGroupService.getDetail(id);
    }

    /**
     * 获取分组树结构
     *
     * @param vo 查询条件
     * @return 权限树
     */
    @GetMapping("/tree")
    public List<TreeNode<PermUnitGroup>> getTree(@Valid PermUnitGroupQueryVO vo) {
        TreeNode<PermUnitGroup> root = permUnitGroupService.listPermTreeRecursively(vo);
        //不必返回根节点
        return root.getChildren();
    }

    /**
     * 获取直接子节点
     * 用于逐级展开
     *
     * @param parentId 父节点ID
     * @return 节点列表
     */
    @GetMapping("/child")
    public List<PermUnitGroup> getDirectChildren(String parentId) {
        PermUnitGroupQueryVO vo = new PermUnitGroupQueryVO();
        vo.setParentId(parentId == null ? "" : parentId);
        return permUnitGroupService.listDirectChildren(vo);
    }

    /**
     * 创建分组节点
     *
     * @param vo 节点信息
     * @return 创建后的节点信息
     */
    @PostMapping
    public PermUnitGroup create(@RequestBody @Valid PermUnitGroupCreateVO vo) {
        return permUnitGroupService.create(vo);
    }

    /**
     * 更新分组节点
     *
     * @param id 节点ID
     * @param vo 更新信息
     * @return 更新后的节点信息
     */
    @PutMapping("/{id}")
    public PermUnitGroup update(@PathVariable String id, @RequestBody @Valid PermUnitGroupUpdateVO vo) {
        return permUnitGroupService.update(vo, id);
    }

    /**
     * 拖动节点
     *
     * @param vo 移动详情
     */
    @PostMapping("/move")
    public void move(@RequestBody @Valid NodeMoveVO vo) {
        permUnitGroupService.move(vo);
    }

    /**
     * 删除分组节点
     *
     * @param id 节点ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        permUnitGroupService.delete(id);
    }
}
