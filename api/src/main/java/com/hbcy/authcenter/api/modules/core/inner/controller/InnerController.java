package com.hbcy.authcenter.api.modules.core.inner.controller;

import com.hbcy.authcenter.api.modules.core.inner.service.InnerService;
import com.hbcy.authcenter.sdk.feign.dto.ApiPermDTO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 供网关访问的内部API
 * 性能比较重要，所以重写了实现逻辑
 * 缓存在网关层，这里不需要写
 *
 * @author 姚泰然
 * @ignore
 * @date 2025-12-31 10:10
 */
@RestController
@RequestMapping("inner/portal/v1")
public class InnerController {
    @Resource
    private InnerService innerService;

    /**
     * 刷新用户权限缓存，供网关调用
     */
    @PostMapping("/user/perm/refresh")
    public void refreshUserPerms() {
        innerService.refreshUserPerms();
    }

    /**
     * 获取所有应用的API路由和权限点的映射
     * 用于网关层建立内存缓存
     */
    @GetMapping("/app/perm")
    public List<ApiPermDTO> listAppPerms() {
        return innerService.listAppPerms();
    }

    /**
     * 获取所有租户的默认管理员
     * 用于网关层建立内存缓存
     *
     * @return key:租户id, value: 管理员用户id
     */
    @GetMapping("/tenant/admin")
    public Map<String, String> getAllTenantAdmin() {
        return innerService.getAllTenantAdmin();
    }
}
