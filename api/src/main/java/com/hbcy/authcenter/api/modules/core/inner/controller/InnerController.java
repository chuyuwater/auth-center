package com.hbcy.authcenter.api.modules.core.inner.controller;

import com.hbcy.authcenter.api.modules.core.auth.model.UserAccess;
import com.hbcy.authcenter.api.modules.core.auth.service.UserAccessService;
import com.hbcy.authcenter.api.modules.core.inner.service.InnerService;
import com.hbcy.authcenter.gateway.dto.ApiPermDTO;
import com.hbcy.authcenter.gateway.vo.RefreshUserPermVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@Validated
@RestController
@RequestMapping("inner/portal/v1")
public class InnerController {
    @Resource
    private InnerService innerService;
    @Resource
    private UserAccessService userAccessService;

    /**
     * 刷新用户权限缓存，供网关调用
     */
    @PostMapping("/user/perm/refresh")
    public void refreshUserPerms(@Valid @RequestBody RefreshUserPermVO vo) {
        innerService.refreshUserPerms(vo);
    }

    /**
     * 获取所有应用的API路由和权限点的映射
     * 用于网关层建立内存缓存
     */
    @GetMapping("/app/perm")
    public List<ApiPermDTO> listAppPerms(String appId) {
        return innerService.listAppPerms(appId);
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

    /**
     * 获取用户密钥详情，用于网关通信
     * @param ak 前端传入的accessKey
     * @return 密钥详情
     */
    @GetMapping("/access-token/check/{ak}")
    public UserAccess checkUserAccess(@PathVariable String ak) {
        return userAccessService.checkUserAccess(ak);
    }
}
