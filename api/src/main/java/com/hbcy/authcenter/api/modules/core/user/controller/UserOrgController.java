package com.hbcy.authcenter.api.modules.core.user.controller;

import com.hbcy.authcenter.api.modules.core.user.service.UserOrgService;
import com.hbcy.authcenter.api.modules.core.user.vo.SwitchDefaultOrgVO;
import com.hbcy.authcenter.api.modules.core.user.vo.UserAddOrgVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户组织关联管理
 *
 * @author 姚泰然
 * @module user
 * @date 2025-12-26 21:46
 */
@RestController
@RequestMapping("/api/portal/v1/user/org")
public class UserOrgController {

    @Resource
    private UserOrgService userOrgService;

    /**
     * 为用户新增组织/部门的关联
     */
    @PostMapping("/add")
    public void addOrg(@Valid @RequestBody UserAddOrgVO vo) {
        userOrgService.addUserNode(vo.getUserId(), vo.getNodeId(), null);
    }

    /**
     * 从组织/部门中移除用户
     *
     * @param id 关联关系的id
     */
    @DeleteMapping("/{id}")
    public void deleteUserOrg(@PathVariable String id) {
        userOrgService.removeUserOrg(id);
    }


    /**
     * 切换主职组织
     */
    @PostMapping("/switch-main")
    public void switchDefaultOrg(@Valid @RequestBody SwitchDefaultOrgVO vo) {
        userOrgService.switchDefaultOrg(vo);
    }
}
