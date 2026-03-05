package com.hbcy.authcenter.api.modules.core.inner.controller;

import com.hbcy.authcenter.api.modules.core.inner.service.SDKService;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitUserService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SDK专用接口
 * 这些接口在网关层不会鉴权，仅供SDK调用
 *
 * @author 姚泰然
 * @date 2026-03-05 09:08
 */
@Validated
@RestController
@RequestMapping("api/portal/v1/sdk")
public class SDKController {

    @Resource
    private SDKService sdkService;

    @Resource
    private PermUnitUserService permUnitUserService;

    /**
     * 获取组织id对应的组织名称
     * @param orgIds 组织id
     * @param fullName 是否返回全称
     * @return 字典
     */
    @GetMapping("/org/names")
    public Map<String, String> getOrgNames(@RequestParam List<String> orgIds, boolean fullName) {
        return sdkService.getOrgNames(orgIds, fullName);
    }

    /**
     * 获取用户id对应的名称
     * @param userIds 组织id
     * @return 字典
     */
    @GetMapping("/user/names")
    public Map<String, String> getOrgNames(@RequestParam Set<String> userIds) {
        return sdkService.getUserNames(userIds);
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
        return sdkService.listGrantOrgs(userId, appId, permCode, parentOrgId);
    }
}
