package com.hbcy.authcenter.api.modules.core.inner.controller;

import com.hbcy.authcenter.api.modules.core.auth.model.UserAccess;
import com.hbcy.authcenter.api.modules.core.auth.service.UserAccessService;
import com.hbcy.authcenter.api.modules.core.inner.service.InnerService;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitUserService;
import com.hbcy.authcenter.gateway.dto.ApiPermDTO;
import com.hbcy.authcenter.gateway.vo.RefreshUserPermVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    @Resource
    private PermUnitUserService permUnitUserService;

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

    /**
     * 获取组织id对应的组织名称
     * @param orgIds 组织id
     * @param fullName 是否返回全称
     * @return 字典
     */
    @GetMapping("/org/names")
    public Map<String, String> getOrgNames(@RequestParam List<String> orgIds, boolean fullName) {
        return innerService.getOrgNames(orgIds, fullName);
    }

    /**
     * 获取用户id对应的名称
     * @param userIds 组织id
     * @return 字典
     */
    @GetMapping("/user/names")
    public Map<String, String> getOrgNames(@RequestParam Set<String> userIds) {
        return innerService.getUserNames(userIds);
    }

    /**
     * 获取用户在指定组织、指定应用下是否有某个权限码
     * @param userId 用户id
     * @param orgId 组织id
     *
     * @return 是否拥有权限
     */
    @GetMapping("/perm/check")
    public boolean hasAnyPerm(@NotBlank(message = "用户id不能为空") String userId,
                              @NotBlank(message = "组织id不能为空") String orgId,
                              @NotBlank(message = "应用id不能为空") String appId,
                              @NotBlank(message = "权限码不能为空") String permCode) {
        return permUnitUserService.checkPerm(userId, orgId, appId, permCode);
    }

    /**
     * 获取用户有指定权限码的组织
     * @param userId 用户id
     * @param appId 应用id
     * @param permCode 权限码
     * @param parentOrgId 父级组织id，查询本下，不传则查询租户所有满足条件的组织
     * @return 组织id列表
     */
    @GetMapping("/perm/grant-orgs")
    public List<String> listGrantOrgs(@NotBlank(message = "用户id不能为空") String userId,
                                      @NotBlank(message = "应用id不能为空") String appId,
                                      @NotBlank(message = "权限码不能为空") String permCode,
                                      String parentOrgId) {
        return innerService.listGrantOrgs(userId, appId, permCode, parentOrgId);
    }
}
