package com.hbcy.authcenter.api.modules.core.tenant.controller;

import com.hbcy.authcenter.api.modules.core.tenant.service.TenantMaintainService;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAdminUpdateVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 维护租户信息（租户侧）
 *
 * @author 姚泰然
 * @module tenant
 * @date 2025-12-31 14:31
 */
@RestController
@RequestMapping("api/portal/v1/tenant/maintain")
public class TenantMaintainController {
    @Resource
    private TenantMaintainService tenantMaintainService;

    /**
     * 修改默认管理员信息
     * 供租户使用
     *
     * @param vo 新的管理员
     */
    @PostMapping("/admin")
    public void updateDefaultAdmin(TenantAdminUpdateVO vo) {
        tenantMaintainService.updateDefaultAdmin(vo);
    }
}
