package com.hbcy.authcenter.api.modules.minor.user.controller;

import com.hbcy.authcenter.api.modules.core.user.dto.UserQueryResultDTO;
import com.hbcy.authcenter.api.modules.core.user.service.UserService;
import com.hbcy.authcenter.api.modules.core.user.vo.UserResetPasswdVO;
import com.hbcy.authcenter.api.modules.core.user.vo.UserUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 我的的个人资料（用户侧）
 *
 * @author 姚泰然
 * @date 2025-12-31 08:33
 */
@RestController
@RequestMapping("api/portal/v1/self")
public class UserProfileController {
    @Resource
    private UserService userService;

    /**
     * 用户修改密码
     */
    @PostMapping("/update-passwd")
    public void userResetPasswd(@Valid @RequestBody UserResetPasswdVO vo) {
        userService.userResetPasswd(vo);
    }

    /**
     * 用户获取自己的个人资料
     */
    @GetMapping("/profile")
    public UserQueryResultDTO getProfile() {
        return userService.getUser(UserContextUtils.getUserId());
    }

    /**
     * 用户修改自己的个人资料
     */
    @PutMapping("/profile")
    public void updateProfile(@RequestBody UserUpdateVO vo) {
        userService.updateUser(UserContextUtils.getUserId(), vo);
    }
}
