package com.hbcy.authcenter.api.modules.core.app.controller;

import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.api.modules.core.app.service.ResourcePermService;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermQueryVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermUpdateVO;
import com.hbcy.common.web.bean.NameFill;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限点管理
 * 目前是通过资源批量管理的，因此这里的api没啥用
 *
 * @author 姚泰然
 * @module app
 * @date 2025-12-24
 */
@RestController
@RequestMapping("api/portal/v1/resource/perm")
@Validated
public class ResourcePermController {

    @Resource
    private ResourcePermService resourcePermService;

    /**
     * 权限点列表
     *
     * @param vo 查询条件
     * @return 权限列表
     */
    @GetMapping
    @NameFill
    public List<ResourcePerm> list(@Valid ResourcePermQueryVO vo) {
        return resourcePermService.list(vo);
    }

    /**
     * 修改权限点
     * @param id 权限点ID
     * @param vo 修改信息
     * @return 修改后的权限点信息
     */
    @PutMapping("/{id}")
    public ResourcePerm update(@PathVariable String id, @RequestBody @Valid ResourcePermUpdateVO vo) {
        return resourcePermService.update(id, vo);
    }

    /**
     * 删除权限点
     * @param id 权限点ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        resourcePermService.delete(id);
    }
}
