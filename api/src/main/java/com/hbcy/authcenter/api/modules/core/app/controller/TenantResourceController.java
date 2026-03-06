package com.hbcy.authcenter.api.modules.core.app.controller;

import com.hbcy.authcenter.api.modules.core.app.dto.GrantAppDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResTreeDTO;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitResourceService;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitUserService;
import com.hbcy.authcenter.api.modules.core.perm.vo.ClientResQueryVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.tree.TreeNode;
import com.hbcy.common.web.bean.NameFill;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 我的应用
 *
 * @author 姚泰然
 * @module app
 * @date 2025-12-30 13:02
 */
@RestController
@RequestMapping("api/portal/v1/granted")
@Validated
public class TenantResourceController {
    @Resource
    private PermUnitUserService permUnitUserService;
    @Resource
    private PermUnitResourceService permUnitResourceService;

    /**
     * 租户查看已授权的应用清单
     * 对应我的应用页面，含被禁用app
     */
    @GetMapping("/app")
    @NameFill
    public List<GrantAppDTO> listGrantApps() {
        return permUnitUserService.listApp(null, true);
    }

    /**
     * 获取当前用户、当前组织下、指定应用的资源树
     * 移除了未授权的节点
     */
    @GetMapping("/app/res-tree")
    public List<TreeNode<ResTreeDTO>> listRes(@NotBlank(message = "应用id不能为空") String appId) {
        ClientResQueryVO vo = new ClientResQueryVO();
        vo.setAppId(appId);
        vo.setOrgId(UserContextUtils.getUserOrg());
        vo.setWithPerm(true);
        return permUnitResourceService.listUserResources(vo);
    }
}
