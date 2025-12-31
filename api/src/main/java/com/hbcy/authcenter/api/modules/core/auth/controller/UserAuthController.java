package com.hbcy.authcenter.api.modules.core.auth.controller;

import com.hbcy.authcenter.api.modules.core.auth.dto.CaptchaDTO;
import com.hbcy.authcenter.api.modules.core.auth.dto.LoginRespDTO;
import com.hbcy.authcenter.api.modules.core.auth.service.UserAuthService;
import com.hbcy.authcenter.api.modules.core.auth.vo.LoginVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口（用户侧）
 *
 * @author 姚泰然
 * @module auth
 * @date 2025-12-30 17:07
 */
@RestController
@RequestMapping("/api/portal/v1/auth")
public class UserAuthController {

    @Resource
    private UserAuthService userAuthService;

    /**
     * 获取验证码
     *
     * @return 验证码
     */
    @GetMapping("/captcha")
    public CaptchaDTO getCaptcha() {
        return userAuthService.getCaptcha();
    }

    /**
     * 登录
     *
     * @param vo 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public LoginRespDTO login(@Valid @RequestBody LoginVO vo) {
        return userAuthService.login(vo);
    }

    /**
     * 登出
     */
    public void logout() {
        userAuthService.logout(null);
    }
}
