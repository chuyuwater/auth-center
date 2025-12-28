package com.hbcy.authcenter.api.modules.core.perm.controller;

import com.hbcy.authcenter.api.common.bean.NodeMoveVO;
import com.hbcy.authcenter.api.modules.core.perm.model.PermTree;
import com.hbcy.authcenter.api.modules.core.perm.service.PermTreeService;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermTreeCreateVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermTreeQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermTreeUpdateVO;
import com.hbcy.common.base.tree.TreeNode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限分组树相关api
 *
 * @author 姚泰然
 * @date 2025-12-28
 */
@RestController
@RequestMapping("api/portal/v1/perm/tree")
@Validated
public class PermTreeController {

    @Resource
    private PermTreeService permTreeService;

    /**
     * 根据ID获取权限节点详情
     *
     * @param id 节点ID
     * @return 节点信息
     */
    @GetMapping("/{id}")
    public PermTree getById(@PathVariable String id) {
        return permTreeService.getById(id);
    }

    /**
     * 获取权限树结构
     *
     * @param vo 查询条件
     * @return 权限树
     */
    @GetMapping
    public List<TreeNode<PermTree>> getTree(@Valid PermTreeQueryVO vo) {
        TreeNode<PermTree> root = permTreeService.listPermTreeRecursively(vo);
        //不必返回根节点
        return root.getChildren();
    }

    @GetMapping("/direct")
    public List<PermTree> getDirectChildren(@Valid PermTreeQueryVO vo) {
        return permTreeService.listDirectChildren(vo);
    }

    /**
     * 创建权限节点
     *
     * @param vo 节点信息
     * @return 创建后的节点信息
     */
    @PostMapping
    public PermTree create(@RequestBody @Valid PermTreeCreateVO vo) {
        return permTreeService.create(vo);
    }

    /**
     * 更新权限节点
     *
     * @param id 节点ID
     * @param vo 更新信息
     * @return 更新后的节点信息
     */
    @PutMapping("/{id}")
    public PermTree update(@PathVariable String id, @RequestBody @Valid PermTreeUpdateVO vo) {
        return permTreeService.update(vo, id);
    }

    /**
     * 拖动节点
     *
     * @param vo 移动详情
     */
    @PostMapping("/move")
    public void move(@RequestBody @Valid NodeMoveVO vo) {
        permTreeService.move(vo);
    }

    /**
     * 删除权限节点
     *
     * @param id 节点ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        permTreeService.delete(id);
    }
}
