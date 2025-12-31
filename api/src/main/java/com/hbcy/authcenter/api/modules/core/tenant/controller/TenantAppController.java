package com.hbcy.authcenter.api.modules.core.tenant.controller;

import com.hbcy.authcenter.api.modules.core.app.dto.GrantAppDTO;
import com.hbcy.authcenter.api.modules.core.tenant.service.TenantAppService;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppGrantUpdateVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppGrantVO;
import com.hbcy.common.web.bean.NameFill;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用授权（门户侧）
 *
 * @author 姚泰然
 * @module tenant
 * @date 2025-12-23 17:19
 */
@RestController
@RequestMapping("api/portal/v1/grant/app")
@Validated
public class TenantAppController {

    @Resource
    private TenantAppService tenantAppService;

    /**
     * 为租户授权应用
     *
     * @param vo 授权信息
     */
    @PostMapping
    public void create(@RequestBody @Valid TenantAppGrantVO vo) {
        tenantAppService.grantApp(vo);
    }

    /**
     * 修改已有的应用授权
     *
     * @param vo 授权详情
     * @param id 授权id
     */
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid TenantAppGrantUpdateVO vo, @PathVariable String id) {
        tenantAppService.updateGrantedApp(id, vo);
    }

    /**
     * 删除授权
     *
     * @param grantId 授权id
     */
    @DeleteMapping("/{grantId}")
    public void deleteGrant(@PathVariable String grantId) {
        tenantAppService.deleteGrant(grantId);
    }

    /**
     * 查看租户已授权的应用列表
     *
     * @param tenantId 租户id
     * @return 应用列表
     */
    @GetMapping("/{tenantId}")
    @NameFill
    public List<GrantAppDTO> listGrantApps(@PathVariable String tenantId) {
        return tenantAppService.listGrantApps(tenantId);
    }
}