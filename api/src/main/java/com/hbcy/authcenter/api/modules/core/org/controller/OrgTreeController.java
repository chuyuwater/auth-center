package com.hbcy.authcenter.api.modules.core.org.controller;

import com.hbcy.authcenter.api.common.pojo.NodeMoveVO;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.service.OrgTreeService;
import com.hbcy.authcenter.api.modules.core.org.vo.OrgTreeCreateVO;
import com.hbcy.authcenter.api.modules.core.org.vo.OrgTreeQueryVO;
import com.hbcy.authcenter.api.modules.core.org.vo.OrgTreeUpdateVO;
import com.hbcy.common.base.tree.TreeNode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织架构树相关api
 *
 * @author 姚泰然
 * @date 2025-12-25
 */
@RestController
@RequestMapping("api/portal/v1/org/tree")
@Validated
public class OrgTreeController {

    @Resource
    private OrgTreeService orgTreeService;

    /**
     * 根据ID获取组织节点详情
     *
     * @param id 节点ID
     * @return 节点信息
     */
    @GetMapping("/{id}")
    public OrgTree getById(@PathVariable String id) {
        return orgTreeService.getById(id);
    }

    /**
     * 获取组织架构树结构
     *
     * @param vo 查询条件
     * @return 组织架构树
     */
    @GetMapping
    public TreeNode<OrgTree> getTree(@Valid OrgTreeQueryVO vo) {
        return orgTreeService.listOrgTreeRecursively(vo);
    }

    @GetMapping("/direct")
    public List<OrgTree> getDirectChildren(@Valid OrgTreeQueryVO vo) {
        return orgTreeService.listDirectChildren(vo);
    }

    /**
     * 创建组织节点
     *
     * @param vo 节点信息
     * @return 创建后的节点信息
     */
    @PostMapping
    public OrgTree create(@RequestBody @Valid OrgTreeCreateVO vo) {
        return orgTreeService.create(vo);
    }

    /**
     * 更新组织节点
     *
     * @param id 节点ID
     * @param vo 更新信息
     * @return 更新后的节点信息
     */
    @PutMapping("/{id}")
    public OrgTree update(@PathVariable String id, @RequestBody @Valid OrgTreeUpdateVO vo) {
        return orgTreeService.update(vo, id);
    }

    /**
     * 拖动节点
     *
     * @param vo 移动详情
     */
    @PostMapping("/move")
    public void move(@RequestBody @Valid NodeMoveVO vo) {
        orgTreeService.move(vo);
    }

    /**
     * 删除组织节点
     *
     * @param id 节点ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        orgTreeService.delete(id);
    }
}
