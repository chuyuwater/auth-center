package com.hbcy.authcenter.api.modules.core.tenant.controller;

import com.hbcy.authcenter.api.modules.core.app.dto.AppCardDTO;
import com.hbcy.authcenter.api.modules.core.app.vo.BindOrgTreeVO;
import com.hbcy.authcenter.api.modules.core.tenant.service.TenantAppService;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppGrantStatusUpdateVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppGrantUpdateVO;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppGrantVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用授权
 *
 * @author 姚泰然
 * @date 2025-12-23 17:19
 */
@RestController
@RequestMapping("api/portal/v1/grant/app")
@Validated
public class TenantAppController {

    @Resource
    private TenantAppService tenantAppService;

    /**
     * 授权应用
     *
     * @param vo 授权信息
     */
    @PostMapping
    public void create(@RequestBody @Valid TenantAppGrantVO vo) {
        tenantAppService.grantApp(vo);
    }

    /**
     * 修改已有的授权详情
     *
     * @param vo 授权详情
     * @param id 授权id
     */
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid TenantAppGrantUpdateVO vo, @PathVariable String id) {
        tenantAppService.updateGrantedApp(id, vo);
    }

    /**
     * 切换授权状态
     *
     * @param vo 状态信息
     */
    @PostMapping("/switch")
    public void switchStatus(@RequestBody @Valid TenantAppGrantStatusUpdateVO vo) {
        tenantAppService.switchGrantStatus(vo);
    }

    /**
     * 删除（撤销）授权
     *
     * @param grantId 授权id
     */
    @DeleteMapping("/{grantId}")
    public void deleteGrant(@PathVariable String grantId) {
        tenantAppService.deleteGrant(grantId);
    }

    /**
     * 查看租户已授权的应用列表（门户侧）
     *
     * @param tenantId 租户id
     * @return 应用列表
     */
    @GetMapping("/{tenantId}")
    public List<AppCardDTO> listGrantApps(@PathVariable String tenantId) {
        return tenantAppService.listGrantApps(tenantId);
    }

    /**
     * 租户查看已授权的应用清单（租户侧）
     */
    @GetMapping
    public List<AppCardDTO> listGrantApps() {
        return tenantAppService.listGrantApps(UserContextUtils.getTenantId());
    }

    /**
     * 租户为应用绑定组织树
     *
     * @param vo 绑定信息
     */
    @PostMapping("/orgTree")
    public void bindOrgTree(@Valid @RequestBody BindOrgTreeVO vo) {
        tenantAppService.bindingOrgTree(vo);
    }
}