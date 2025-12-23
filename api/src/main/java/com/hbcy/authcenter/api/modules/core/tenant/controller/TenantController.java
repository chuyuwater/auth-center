package com.hbcy.authcenter.api.modules.core.tenant.controller;

import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.tenant.service.TenantService;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantForbiddenVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantQueryVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantUpsertVO;
import com.hbcy.common.db.model.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 租户相关api
 *
 * @author 姚泰然
 * @date 2025-12-23 12:06
 */
@RestController
@RequestMapping("api/portal/v1/tenant")
@Validated
public class TenantController {

    @Resource
    private TenantService tenantService;

    /**
     * 分页查询租户列表
     *
     * @param vo 查询条件
     * @return 租户分页列表
     */
    @GetMapping
    public PageResp<Tenant> list(@RequestBody @Valid TenantQueryVO vo) {
        return tenantService.list(vo);
    }

    /**
     * 根据ID获取租户信息
     *
     * @param id 租户ID
     * @return 租户信息
     */
    @GetMapping("/{id}")
    public Tenant getById(@PathVariable String id) {
        return tenantService.getById(id);
    }

    /**
     * 创建租户
     *
     * @param vo 租户信息
     * @return 创建后的租户信息
     */
    @PostMapping
    public Tenant create(@RequestBody @Valid TenantUpsertVO vo) {
        return tenantService.create(vo);
    }

    /**
     * 更新租户
     *
     * @param id 租户ID
     * @param vo 租户信息
     * @return 更新后的租户信息
     */
    @PutMapping("/{id}")
    public Tenant update(@PathVariable String id, @RequestBody @Valid TenantUpsertVO vo) {
        return tenantService.update(vo, id);
    }

    /**
     * 切换租户状态（禁用/启用）
     *
     * @param vo 状态切换信息
     */
    @PostMapping("/status")
    public void switchStatus(@RequestBody @Valid TenantForbiddenVO vo) {
        tenantService.switchStatus(vo);
    }

    /**
     * 删除租户
     *
     * @param id 租户ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        tenantService.delete(id);
    }
}
