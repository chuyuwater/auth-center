package com.hbcy.authcenter.api.modules.core.user.controller;

import com.hbcy.authcenter.api.modules.core.user.service.UserOrgService;
import com.hbcy.authcenter.api.modules.core.user.vo.UserAddOrgVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * @author 姚泰然
 * @date 2025-12-26 21:46
 */
@RestController
@RequestMapping("/api/portal/v1/user/org")
public class UserOrgController {

    @Resource
    private UserOrgService userOrgService;

    /**
     * 为用户新增组织/部门
     */
    @PostMapping("/add")
    public void addOrg(@Valid @RequestBody UserAddOrgVO vo) {
        userOrgService.addUserNode(vo.getUserId(), vo.getNodeId());
    }

    /**
     * 从组织/部门中移除用户
     *
     * @param id 关联关系的id
     */
    @DeleteMapping("/{id}")
    public void deleteUserOrg(@PathVariable String id) {
        userOrgService.removeUserNode(id);
    }
}
