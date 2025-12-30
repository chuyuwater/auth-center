package com.hbcy.authcenter.api.modules.core.auth.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.core.auth.dto.CaptchaDTO;
import com.hbcy.authcenter.api.modules.core.auth.dto.LoginRespDTO;
import com.hbcy.authcenter.api.modules.core.auth.vo.LoginVO;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.core.user.service.UserService;
import com.hbcy.common.base.error.ClientError;
import com.hbcy.common.base.error.ParamError;
import com.pig4cloud.captcha.ArithmeticCaptcha;
import com.pig4cloud.captcha.base.Captcha;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 姚泰然
 * @date 2025-12-30 17:08
 */
@Service
public class UserAuthService {

    public static final String CAPTCHA_KEY_PREFIX = "portal:captcha:";
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public LoginRespDTO login(LoginVO vo) {
        if (StringUtils.isAllBlank(vo.getAccount(), vo.getPhone(), vo.getEmail())) {
            throw new ParamError("请输入账号/手机号/邮箱");
        }
        String captchaCode = getCaptchaCode(vo.getCaptchaId());
        if (StringUtils.isBlank(captchaCode)) {
            throw new ParamError("验证码已过期");
        }
        if (!captchaCode.equals(vo.getCaptchaCode())) {
            throw new ParamError("验证码错误");
        }
        List<User> userList = userService.list(new QueryWrapper<User>()
                .eq(StringUtils.isNotBlank(vo.getAccount()), User.COL_ACCOUNT, vo.getAccount())
                .eq(StringUtils.isNotBlank(vo.getPhone()), User.COL_PHONE, vo.getPhone())
                .eq(StringUtils.isNotBlank(vo.getEmail()), User.COL_EMAIL, vo.getEmail())
                .eq(StringUtils.isNotBlank(vo.getTenantId()), User.COL_TENANT_ID, vo.getTenantId())
        );
        if (CollectionUtils.isEmpty(userList)) {
            throw new ParamError("账号或密码错误");
        }
        List<String> hitTenant = new ArrayList<>();
        User chosen = null;
        for (User user : userList) {
            if (passwordEncoder.matches(vo.getPassword(), user.getPasswd())) {
                hitTenant.add(user.getTenantId());
            }
        }
        if (CollectionUtils.isEmpty(hitTenant)) {
            throw new ParamError("账号或密码错误");
        }
        if (hitTenant.size() > 1) {
            throw new ClientError(100, "请选择租户", hitTenant);
        }
        chosen = userList.get(0);
        if (Integer.valueOf(1).equals(chosen.getForbidden())) {
            throw new ParamError("账号已被禁用，请联系管理员");
        }
        // 执行登录
        StpUtil.login(chosen.getId(), new SaLoginParameter()
        );
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        LoginRespDTO resp = new LoginRespDTO()
                .setToken(tokenInfo.tokenValue)
                .setLastLoginTime(chosen.getLastLogin())
                .setUserId(chosen.getId())
                .setRealName(chosen.getRealName())
                .setAvatar(chosen.getAvatar());
        // 更新最后登录时间
        chosen.setLastLogin(LocalDateTime.now());
        userService.updateById(chosen);
        return resp;
    }

    public void logout() {
        StpUtil.logout();
    }

    private String getCaptchaCode(String key) {
        return stringRedisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + key);
    }

    public CaptchaDTO getCaptcha() {
        Captcha captcha = new ArithmeticCaptcha(130, 48);
        String verCode = captcha.text();
        String key = UlidCreator.getUlid().toString();
        // 存入redis并设置过期时间为30分钟
        stringRedisTemplate.opsForValue().set(CAPTCHA_KEY_PREFIX + key, verCode, 1, TimeUnit.MINUTES);
        // 将key和base64返回给前端
        return new CaptchaDTO(key, captcha.toBase64());
    }
}
