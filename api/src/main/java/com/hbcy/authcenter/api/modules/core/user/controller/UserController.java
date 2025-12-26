package com.hbcy.authcenter.api.modules.core.user.controller;

import com.hbcy.authcenter.api.modules.core.user.service.UserService;
import com.hbcy.authcenter.api.modules.core.user.vo.*;
import com.hbcy.common.base.pojo.BatchDeleteVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理
 *
 * @author 姚泰然
 * @date 2025-12-26
 */
@RestController
@RequestMapping("/api/portal/v1/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    public String createUser(@Valid @RequestBody CreateUserVO vo) {
        return userService.createUser(vo);
    }

    /**
     * 修改用户
     */
    @PutMapping("/{userId}")
    public void updateUser(@PathVariable String userId, @Valid @RequestBody UpdateUserVO vo) {
        userService.updateUser(userId, vo);
    }

    /**
     * 删除用户
     *
     * @param userId 用户id
     */
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
    }

    /**
     * 批量删除
     *
     * @param vo 用户id列表
     */
    @PostMapping("/batch-delete")
    public void deleteUsers(@Valid @RequestBody BatchDeleteVO vo) {
        userService.deleteUsers(vo);
    }

    /**
     * 禁用/解禁用户
     */
    @PatchMapping("/forbidden")
    public void forbidUser(@Valid @RequestBody ForbidUserVO vo) {
        userService.forbidUser(vo);
    }

    /**
     * 管理员重置密码
     */
    @PatchMapping("/admin-reset-passwd")
    public void adminResetPasswd(@Valid @RequestBody AdminResetPasswdVO vo) {
        userService.adminResetPasswd(vo);
    }

    /**
     * 用户重置密码
     */
    @PatchMapping("/user-reset-passwd")
    public void userResetPasswd(@Valid @RequestBody UserResetPasswdVO vo) {
        userService.userResetPasswd(vo);
    }

    /**
     * 切换默认组织
     */
    @PatchMapping("/switch-default-org")
    public void switchDefaultOrg(@Valid @RequestBody SwitchDefaultOrgVO vo) {
        userService.switchDefaultOrg(vo);
    }
}
