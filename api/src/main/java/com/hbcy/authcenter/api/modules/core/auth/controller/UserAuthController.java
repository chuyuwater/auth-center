package com.hbcy.authcenter.api.modules.core.auth.controller;

import com.hbcy.authcenter.api.modules.core.auth.dto.CaptchaDTO;
import com.hbcy.authcenter.api.modules.core.auth.dto.LoginRespDTO;
import com.hbcy.authcenter.api.modules.core.auth.service.UserAuthService;
import com.hbcy.authcenter.api.modules.core.auth.vo.LoginVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * @author 姚泰然
 * @date 2025-12-30 17:07
 */
@RestController
@RequestMapping("/api/portal/v1/auth")
public class UserAuthController {

    @Resource
    private UserAuthService userAuthService;

    @GetMapping("/captcha")
    public CaptchaDTO getCaptcha() {
        return userAuthService.getCaptcha();
    }

    @PostMapping("/login")
    public LoginRespDTO login(@Valid @RequestBody LoginVO vo) {
        return userAuthService.login(vo);
    }

    @PostMapping("/logout")
    public void logout() {
        userAuthService.logout(null);
    }
}
