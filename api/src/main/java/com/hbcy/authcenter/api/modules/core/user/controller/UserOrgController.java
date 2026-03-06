package com.hbcy.authcenter.api.modules.core.user.controller;

import com.hbcy.authcenter.api.modules.core.user.service.UserOrgService;
import com.hbcy.authcenter.api.modules.core.user.vo.SwitchDefaultOrgVO;
import com.hbcy.authcenter.api.modules.core.user.vo.UserAddOrgVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户组织关联管理（租户侧）
 *
 * @author 姚泰然
 * @module user
 * @date 2025-12-26 21:46
 */
@RestController
@RequestMapping("/api/portal/v1/user/org")
@Validated
public class UserOrgController {

    @Resource
    private UserOrgService userOrgService;

    /**
     * 为用户新增组织/部门的关联
     */
    @PostMapping("/add")
    public void addOrg(@Valid @RequestBody UserAddOrgVO vo) {
        userOrgService.addUserNode(vo.getUserId(), vo.getNodeIds());
    }

    /**
     * 从组织/部门中移除用户
     * @param userId 用户id
     * @param nodeId 用户实际挂载的节点（部门或组织）id
     */
    @PostMapping("/delete")
    public void deleteUserOrg(@NotBlank(message = "节点id不能为空") @RequestParam String nodeId,
                              @NotBlank(message = "用户id不能为空") @RequestParam String userId) {
        userOrgService.removeUserOrg(userId, nodeId);
    }


    /**
     * 切换主职组织
     */
    @PostMapping("/switch-main")
    public void switchDefaultOrg(@Valid @RequestBody SwitchDefaultOrgVO vo) {
        userOrgService.switchDefaultOrg(vo);
    }
}
