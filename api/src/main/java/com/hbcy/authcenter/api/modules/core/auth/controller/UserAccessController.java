package com.hbcy.authcenter.api.modules.core.auth.controller;

import com.hbcy.authcenter.api.modules.core.auth.model.TenantAccess;
import com.hbcy.authcenter.api.modules.core.auth.service.TenantAccessService;
import com.hbcy.authcenter.api.modules.core.auth.vo.TenantAccessUpsertVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.PermissionError;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AK认证
 * 仅租户管理员可用
 * @author 姚泰然
 * @module auth
 * @date 2026-01-26 19:30
 */
@RestController
@RequestMapping("/api/portal/v1/user/access-token")
public class UserAccessController {
    @Resource
    private TenantAccessService tenantAccessService;

    /**
     * 创建访问密钥
     * @param vo 信息
     * @return 密钥信息
     */
    @PostMapping
    public TenantAccess createUserAccess(@Valid @RequestBody TenantAccessUpsertVO vo) {
        if (!UserContextUtils.isTenantAdmin()) {
            throw new PermissionError("只有租户管理员才能创建ak");
        }
        return tenantAccessService.createAccess(vo);
    }

    /**
     * 更新访问密钥
     * @param id 密钥ID
     * @param vo 信息
     * @return 密钥信息
     */
    @PutMapping("/{id}")
    public TenantAccess updateUserAccess(@PathVariable String id, @Valid @RequestBody TenantAccessUpsertVO vo) {
        if (!UserContextUtils.isTenantAdmin()) {
            throw new PermissionError("只有租户管理员才能修改ak");
        }
        return tenantAccessService.updateAccess(id, vo);
    }

    /**
     * 删除访问密钥
     * @param id 密钥id
     */
    @PostMapping("/delete")
    public void deleteUserAccess(@NotBlank(message = "id不能为空") String id) {
        if (!UserContextUtils.isTenantAdmin()) {
            throw new PermissionError("只有租户管理员才能删除ak");
        }
        tenantAccessService.deleteAccess(id);
    }

    /**
     * 密钥详情
     * @param id 密钥id
     * @return 密钥详情
     */
    @GetMapping("/{id}")
    public TenantAccess getUserAccess(@PathVariable String id) {
        if (!UserContextUtils.isTenantAdmin()) {
            throw new PermissionError();
        }
        return tenantAccessService.getById(id);
    }

    /**
     * 获取租户的所有密钥
     * @return 密钥列表
     */
    @GetMapping
    public List<TenantAccess> listUserAccess() {
        if (!UserContextUtils.isTenantAdmin()) {
            throw new PermissionError();
        }
        return tenantAccessService.getTenantAKs();
    }
}
