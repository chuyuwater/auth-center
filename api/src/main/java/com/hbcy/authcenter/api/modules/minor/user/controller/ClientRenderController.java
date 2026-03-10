package com.hbcy.authcenter.api.modules.minor.user.controller;

import com.hbcy.authcenter.api.common.enums.ClientTypeEnum;
import com.hbcy.authcenter.api.modules.core.app.dto.AppCardDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.GrantAppDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResPermDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResTreeDTO;
import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitResourceService;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitUserService;
import com.hbcy.authcenter.api.modules.core.perm.vo.ClientResQueryVO;
import com.hbcy.authcenter.api.modules.core.user.dto.UserOrgDTO;
import com.hbcy.authcenter.api.modules.minor.user.service.ClientRenderService;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.tree.TreeNode;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
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
        int maxDepth = clientType == ClientTypeEnum.MOBILE ? 2 : 1;
        return clientRenderService.listUserMenu(
                UserContextUtils.getUserId(), UserContextUtils.getUserOrg(), clientType, maxDepth
        );
    }

    /**
     * 获取当前用户的完整菜单树，用于构建快捷入口
     * @param clientType 1:PC端 2:移动端
     * @return 菜单树
     */
    @GetMapping("/menu-tree")
    public List<TreeNode<ResourceTree>> listMenuTree(
            @NotNull(message = "clientType必须指定") Integer clientType) {
        return clientRenderService.listUserMenu(
                UserContextUtils.getUserId(), UserContextUtils.getUserOrg(), clientType, 0
        );
    }

    /**
     * 获取用户可以切换的组织
     * 仅返回用户加入的组织级别数据，且移除了已被禁用的组织
     *
     * @return 组织树
     */
    @GetMapping("/org-tree")
    public List<TreeNode<UserOrgDTO>> listOrgTree() {
        return clientRenderService.listUserOrgTree();
    }

    /**
     * 获取当前用户、当前组织下、当前应用的资源树（菜单）
     */
    @GetMapping("/res")
    public List<TreeNode<ResTreeDTO>> listRes(ClientResQueryVO vo) {
        vo.setOrgId(UserContextUtils.getUserOrg());
        if (StringUtils.isBlank(vo.getAppId())) {
            vo.setAppId(UserContextUtils.getAppId());
        }
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
        return permUnitUserService.listPerms(UserContextUtils.getAppId(), resId);
    }

    /**
     * 获取当前用户、当前组织下、当前应用（或指定菜单下）的权限码
     * 该结果不会使用缓存，因此可能和网关侧的权限判断结果不一致
     *
     * @param customId 前端自定义的菜单id（非数据库id）， 不传则返回用户拥有的整个app的所有权限码
     * @return 权限码
     */
    @GetMapping("/perm-code")
    public Set<String> listPermCode(String customId) {
        List<ResPermDTO> dtos = permUnitUserService.listPermByCustomId(
                UserContextUtils.getAppId(), customId);
        return dtos.stream().map(ResPermDTO::getPermCode).collect(Collectors.toSet());
    }

    /**
     * 判断当前用户在当前组织、当前app下是否有某个权限码
     * 会使用与网关判断权限一致的缓存
     *
     * @param permCode 权限码
     * @return true or false
     */
    @GetMapping("/perm-check")
    public boolean checkPerm(String permCode) {
        return permUnitUserService.checkPerm(permCode);
    }
}
