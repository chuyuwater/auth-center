package com.hbcy.authcenter.api.modules.core.auth.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.config.UserAuthConfig;
import com.hbcy.authcenter.api.modules.core.auth.dto.CaptchaDTO;
import com.hbcy.authcenter.api.modules.core.auth.dto.LoginRespDTO;
import com.hbcy.authcenter.api.modules.core.auth.vo.LoginVO;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.dao.UserOrgMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.gateway.constants.GatewayConstants;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.AuthError;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author 姚泰然
 * @date 2025-12-30 17:08
 */
@Service
public class UserAuthService {

    public static final String CAPTCHA_KEY_PREFIX = "portal:captcha:";
    public static final String USER_LOCK_KEY_PREFIX = "portal:auth:login:lock:";
    public static final String USER_LOGIN_FAIL_KEY_PREFIX = "portal:auth:login:fail:";
    //用户权限缓存（按orgId）
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Resource
    private UserMapper userMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserAuthConfig authConfig;
    @Resource
    private UserOrgMapper userOrgMapper;

    private long checkLockTime(String userId) {
        Long expire = stringRedisTemplate.getExpire(USER_LOCK_KEY_PREFIX + userId, TimeUnit.SECONDS);
        if (expire == null) {
            return 0;
        }
        return expire;
    }

    private boolean checkLoginFail(String userId) {
        String key = USER_LOGIN_FAIL_KEY_PREFIX + userId;
        Long cnt = stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, authConfig.getRetryTime());
        return cnt != null && cnt >= authConfig.getMaxRetry();
    }

    private void lockUser(String userId) {
        stringRedisTemplate.opsForValue().set(USER_LOCK_KEY_PREFIX + userId, "1",
                authConfig.getLockTime());
    }

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
        List<User> userList = userMapper.selectList(new QueryWrapper<User>()
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
            } else {
                if (checkLoginFail(user.getId())) {
                    lockUser(user.getId());
                    throw new AuthError("登录失败次数过多，请稍后再试");
                }
            }
        }
        if (CollectionUtils.isEmpty(hitTenant)) {
            throw new AuthError("账号或密码错误");
        }
        if (hitTenant.size() > 1) {
            throw new ClientError(100, "请选择租户", hitTenant);
        }
        chosen = userList.get(0);
        long expire = checkLockTime(chosen.getId());
        if (expire > 0) {
            throw new AuthError("登录被锁定，请等待%d秒".formatted(expire));
        }
        if (Integer.valueOf(1).equals(chosen.getForbidden())) {
            throw new AuthError("账号已被禁用，请联系管理员");
        }
        // 执行登录
        StpUtil.login(chosen.getId(), new SaLoginParameter()
                .setTimeout(authConfig.getTokenExpire().toSeconds())
                .setActiveTimeout(authConfig.getTokenExpire().toSeconds())
        );
        //将租户id保存到session中
        StpUtil.getSession(true).set(GatewayConstants.SESSION_TENANT_ID, chosen.getTenantId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        LoginRespDTO resp = new LoginRespDTO()
                .setToken(tokenInfo.tokenValue)
                .setLastLoginTime(chosen.getLastLogin())
                .setUserId(chosen.getId())
                .setRealName(chosen.getRealName())
                .setAvatar(chosen.getAvatar());
        // 更新最后登录时间
        chosen.setLastLogin(LocalDateTime.now());
        userMapper.updateById(chosen);
        return resp;
    }

    public void logout(String userId) {
        if (StringUtils.isBlank(userId)) {
            userId = UserContextUtils.getUserId();
        }
        Set<String> userOrgs = userOrgMapper.listAllOrg(userId);
        Set<String> keys = new HashSet<>();
        //强制移除权限缓存
        for (String orgId : userOrgs) {
            keys.add(GatewayConstants.USER_PERM_CACHE_PREFIX.formatted(userId, orgId));
        }
        stringRedisTemplate.delete(keys);
        StpUtil.logout(userId);
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
