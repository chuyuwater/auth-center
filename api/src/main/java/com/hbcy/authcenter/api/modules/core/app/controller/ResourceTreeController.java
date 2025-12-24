package com.hbcy.authcenter.api.modules.core.app.controller;

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


    @GetMapping("/{id}")
    public ResourceTree getById(@PathVariable String id) {
        return resourceTreeService.getById(id);
    }

    @GetMapping
    public TreeNode<ResTreeDTO> getTree(@Valid ResourceTreeQueryVO vo) {
        return resourceTreeService.listResTreeRecursively(vo);
    }

    @PostMapping
    public ResourceTree create(@RequestBody @Valid ResourceTreeCreateVO vo) {
        return resourceTreeService.create(vo);
    }

    @PutMapping("/{id}")
    public ResourceTree update(@PathVariable String id, @RequestBody @Valid ResourceTreeUpdateVO vo) {
        return resourceTreeService.update(vo, id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        resourceTreeService.delete(id);
    }
}
