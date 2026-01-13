package com.hbcy.authcenter.api.modules.core.perm.controller;

import com.hbcy.authcenter.api.modules.core.perm.model.PermUnit;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitService;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitCreateVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitForbidVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUpdateVO;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.web.bean.NameFill;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理（租户侧）
 *
 * @author 姚泰然
 * @module perm
 * @date 2025-12-28
 */
@RestController
@RequestMapping("api/portal/v1/perm/unit")
@Validated
public class PermUnitController {

    @Resource
    private PermUnitService permUnitService;

    /**
     * 权限单元详情
     *
     * @param id 权限单元ID
     * @return 权限单元信息
     */
    @GetMapping("/{id}")
    @NameFill
    public PermUnit getById(@PathVariable String id) {
        return permUnitService.getById(id);
    }

    /**
     * 权限单元列表
     *
     * @param vo 查询条件
     * @return 权限单元列表
     */
    @GetMapping
    @NameFill
    public PageResp<PermUnit> list(@Valid PermUnitQueryVO vo) {
        return permUnitService.list(vo);
    }

    /**
     * 创建权限单元
     *
     * @param vo 权限单元信息
     * @return 创建后的权限单元信息
     */
    @PostMapping
    public PermUnit create(@RequestBody @Valid PermUnitCreateVO vo) {
        return permUnitService.create(vo);
    }

    /**
     * 更新权限单元
     *
     * @param id 权限单元ID
     * @param vo 更新信息
     * @return 更新后的权限单元信息
     */
    @PutMapping("/{id}")
    public PermUnit update(@PathVariable String id, @RequestBody @Valid PermUnitUpdateVO vo) {
        return permUnitService.update(vo, id);
    }

    /**
     * 启用/禁用权限单元
     */
    @PostMapping("/forbidden")
    public void forbid(@Valid @RequestBody PermUnitForbidVO vo) {
        permUnitService.forbid(vo);
    }

    /**
     * 删除权限单元
     *
     * @param id 权限单元ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        permUnitService.delete(id);
    }
}
