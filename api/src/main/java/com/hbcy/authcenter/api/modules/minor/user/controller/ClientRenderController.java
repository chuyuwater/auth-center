package com.hbcy.authcenter.api.modules.minor.user.controller;

import com.hbcy.authcenter.api.modules.core.app.dto.AppCardDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.GrantAppDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResPermDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResTreeDTO;
import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitResourceService;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitUserService;
import com.hbcy.authcenter.api.modules.core.perm.vo.ClientResQueryVO;
import com.hbcy.authcenter.api.modules.minor.user.service.ClientRenderService;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.tree.TreeNode;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 前端菜单渲染
 * <p>
 * 1. 根据当前用户权限，获取应用列表
 * 2. 根据当前用户的权限，和当前应用，获取菜单列表
 * 3. 获取指定菜单下的权限点列表
 * <p>
 * 本页提供的接口不限制权限，仅需要登录即可访问
 *
 * @author 姚泰然
 * @module open
 * @date 2025-12-30 08:36
 */
@RestController
@Validated
@RequestMapping("api/portal/v1/client")
public class ClientRenderController {
    @Resource
    private PermUnitUserService permUnitUserService;
    @Resource
    private PermUnitResourceService permUnitResourceService;
    @Resource
    private ClientRenderService clientRenderService;

    /**
     * 获取当前用户有权访问的app列表
     *
     * @return app列表
     */
    @GetMapping("/app")
    public List<AppCardDTO> listApp() {
        List<GrantAppDTO> apps = permUnitUserService.listApp(null, false);
        return new ArrayList<>(apps);
    }

    /**
     * 获取当前用户的入口菜单
     * 如果是PC端，仅返回一级菜单
     * 如果是移动端，返回一级+二级菜单
     *
     * @param clientType 1:PC端 2:移动端
     */
    @GetMapping("/entry")
    public List<TreeNode<ResourceTree>> entry(@NotNull(message = "clientType必须指定") Integer clientType) {
        return clientRenderService.listEntry(UserContextUtils.getUserId(), UserContextUtils.getUserOrg(), clientType);
    }

    /**
     * 获取当前用户、当前组织下、当前应用的资源树（菜单）
     */
    @GetMapping("/res")
    public List<TreeNode<ResTreeDTO>> listRes(ClientResQueryVO vo) {
        vo.setAppId(UserContextUtils.getAppId());
        vo.setOrgId(UserContextUtils.getUserOrg());
        return permUnitResourceService.listUserResources(vo);
    }

    /**
     * 获取当前用户、当前组织下、当前应用（或指定菜单下）的权限点列表
     *
     * @param resId 菜单id，不传则返回整个app的所有权限点
     * @return 权限点
     */
    @GetMapping("/perm")
    public List<ResPermDTO> listPerm(String resId) {
        return permUnitUserService.listPerms(null, resId);
    }

    /**
     * 获取当前用户、当前组织下、当前应用（或指定菜单下）的权限码
     *
     * @param resId 菜单id， 不传则返回整个app的所有权限码
     * @return 权限码
     */
    @GetMapping("/permCode")
    public Set<String> listPermCode(String resId) {
        List<ResPermDTO> dtos = permUnitUserService.listPerms(null, resId);
        return dtos.stream().map(ResPermDTO::getPermCode).collect(Collectors.toSet());
    }
}
