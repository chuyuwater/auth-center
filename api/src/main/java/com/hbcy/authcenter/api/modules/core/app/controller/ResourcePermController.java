package com.hbcy.authcenter.api.modules.core.app.controller;

import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.api.modules.core.app.service.ResourcePermService;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermCreateVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermQueryVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermUpdateVO;
import com.hbcy.common.web.bean.NameFill;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限点管理（门户侧）
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
     * 查询资源权限列表
     *
     * @param vo 查询条件
     * @return 权限列表
     */
    @GetMapping
    @NameFill
    public List<ResourcePerm> list(@RequestBody @Valid ResourcePermQueryVO vo) {
        return resourcePermService.list(vo);
    }

    /**
     * 根据ID获取权限详情
     *
     * @param id 权限ID
     * @return 权限信息
     */
    @GetMapping("/{id}")
    @NameFill
    public ResourcePerm getById(@PathVariable String id) {
        return resourcePermService.getById(id);
    }

    /**
     * 创建权限点
     *
     * @param vo 权限信息
     * @return 创建后的权限信息
     */
    @PostMapping
    public ResourcePerm create(@RequestBody @Valid ResourcePermCreateVO vo) {
        return resourcePermService.create(vo);
    }

    /**
     * 更新权限点
     *
     * @param id 权限ID
     * @param vo 更新信息
     * @return 更新后的权限信息
     */
    @PutMapping("/{id}")
    public ResourcePerm update(@PathVariable String id, @RequestBody @Valid ResourcePermUpdateVO vo) {
        return resourcePermService.update(vo, id);
    }

    /**
     * 删除权限点
     *
     * @param id 权限ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        resourcePermService.delete(id);
    }
}
