package com.hbcy.authcenter.api.modules.core.app.controller;

import com.hbcy.authcenter.api.common.bean.NodeMoveVO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResTreeDTO;
import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
import com.hbcy.authcenter.api.modules.core.app.service.ResourceTreeService;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourceTreeCreateVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourceTreeQueryVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourceTreeUpdateVO;
import com.hbcy.common.base.tree.TreeNode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 资源树（菜单）相关api
 *
 * @author 姚泰然
 * @date 2025-12-24
 */
@RestController
@RequestMapping("api/portal/v1/resource/tree")
@Validated
public class ResourceTreeController {

    @Resource
    private ResourceTreeService resourceTreeService;

    /**
     * 根据ID获取资源节点详情
     *
     * @param id 节点ID
     * @return 节点信息
     */
    @GetMapping("/{id}")
    public ResourceTree getById(@PathVariable String id) {
        return resourceTreeService.getById(id);
    }

    /**
     * 获取资源树结构
     *
     * @param vo 查询条件
     * @return 资源树
     */
    @GetMapping
    public TreeNode<ResTreeDTO> getTree(@Valid ResourceTreeQueryVO vo) {
        return resourceTreeService.listResTreeRecursively(vo);
    }

    /**
     * 创建资源节点（菜单）
     *
     * @param vo 节点信息
     * @return 创建后的节点信息
     */
    @PostMapping
    public ResourceTree create(@RequestBody @Valid ResourceTreeCreateVO vo) {
        return resourceTreeService.create(vo);
    }

    /**
     * 更新资源节点（菜单）
     *
     * @param id 节点ID
     * @param vo 更新信息
     * @return 更新后的节点信息
     */
    @PutMapping("/{id}")
    public ResourceTree update(@PathVariable String id, @RequestBody @Valid ResourceTreeUpdateVO vo) {
        return resourceTreeService.update(vo, id);
    }

    /**
     * 拖动节点
     *
     * @param vo 移动详情
     */
    @PostMapping("/move")
    public void move(@RequestBody @Valid NodeMoveVO vo) {
        resourceTreeService.move(vo);
    }

    /**
     * 删除资源节点（及其子节点和关联权限）
     *
     * @param id 节点ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        resourceTreeService.delete(id);
    }
}
