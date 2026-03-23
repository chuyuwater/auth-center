package com.hbcy.authcenter.api.modules.core.auth.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.config.UserAuthConfig;
import com.hbcy.authcenter.api.modules.core.auth.dto.CaptchaDTO;
import com.hbcy.authcenter.api.modules.core.auth.dto.LoginRespDTO;
import com.hbcy.authcenter.api.modules.core.auth.vo.LoginVO;
import com.hbcy.authcenter.api.modules.core.inner.service.InnerService;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.dao.UserOrgMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.gateway.constants.GatewayConstants;
import com.hbcy.authcenter.gateway.vo.RefreshUserPermVO;
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
import java.util.*;
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
    @Resource
    private TenantMapper tenantMapper;
    @Resource
    private InnerService innerService;

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

    private void cleanLoginFail(String userId) {
        stringRedisTemplate.delete(USER_LOGIN_FAIL_KEY_PREFIX + userId);
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
        if (!captchaCode.equalsIgnoreCase(vo.getCaptchaCode())) {
            deleteCaptcha(vo.getCaptchaId());
            throw new ParamError("验证码错误");
        }
        if (StringUtils.isNotBlank(vo.getTenantId())) {
            Tenant tenant = tenantMapper.selectById(vo.getTenantId());
            if (!Objects.equals(0, tenant.getForbidden())) {
                throw new ParamError("租户被禁用");
            }
        }
        //NOTE: 如果用户是多租户的，选择租户需要复用验证码，所以不要删除验证码
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
        List<String> tried = new ArrayList<>();
        for (User user : userList) {
            if (passwordEncoder.matches(vo.getPassword(), user.getPasswd())) {
                hitTenant.add(user.getTenantId());
            } else {
                tried.add(user.getId());
            }
        }
        if (CollectionUtils.isEmpty(hitTenant)) {
            for (String userId : tried) {
                if (checkLoginFail(userId)) {
                    lockUser(userId);
                    throw new AuthError("登录失败次数过多，请稍后再试");
                }
            }
            throw new AuthError("账号或密码错误");
        }
        if (hitTenant.size() > 1) {
            List<Tenant> tenantList = tenantMapper.selectByIds(hitTenant);
            ClientError error = new ClientError(100, "请选择租户");
            error.setData(tenantList);
            throw error;
        }
        for (User user : userList) {
            if (user.getTenantId().equals(hitTenant.get(0))) {
                chosen = user;
                break;
            }
        }
        long expire = checkLockTime(chosen.getId());
        if (expire > 0) {
            throw new AuthError("登录被锁定，请等待%d秒".formatted(expire));
        }
        if (Integer.valueOf(1).equals(chosen.getForbidden())) {
            throw new AuthError("账号已被禁用，请联系管理员");
        }
        cleanLoginFail(chosen.getId());
        //主职组织
        String orgId = userOrgMapper.queryMainOrg(chosen.getId());
        if (StringUtils.isBlank(orgId)) {
            throw new AuthError("您所在的组织已被禁用，请联系管理员！");
        }
        //刷新权限
        RefreshUserPermVO refreshUserPermVO = new RefreshUserPermVO()
                .setUserId(chosen.getId())
                .setOrgId(orgId)
                .setTenantId(chosen.getTenantId());
        innerService.refreshUserPerms(refreshUserPermVO);
        //执行登录
        StpUtil.login(chosen.getId());
        //将租户id和用户名字保存到session中
        StpUtil.getSession(true)
                .set(GatewayConstants.SESSION_TENANT_ID, chosen.getTenantId())
                .set(GatewayConstants.SESSION_USER_NAME, chosen.getRealName());

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        Tenant tenant = tenantMapper.selectById(chosen.getTenantId());
        LoginRespDTO resp = new LoginRespDTO()
                .setToken(tokenInfo.tokenValue)
                .setLastLoginTime(chosen.getLastLogin())
                .setUserId(chosen.getId())
                .setTenantId(chosen.getTenantId())
                .setAdminFlag(tenant.getAdminId().equals(chosen.getId()) ? 1 : 0)
                .setOrgId(orgId)
                .setRealName(chosen.getRealName())
                .setAvatar(chosen.getAvatar());
        // 更新最后登录时间
        chosen.setLastLogin(LocalDateTime.now());
        userMapper.update(new UpdateWrapper<User>()
                .eq(User.COL_ID, chosen.getId())
                .set(User.COL_LAST_LOGIN, LocalDateTime.now()));
        return resp;
    }

    public void logout(String userId) {
        if (StringUtils.isBlank(userId)) {
            userId = UserContextUtils.getUserId();
        } else {
            //明确传入用户id时，强制所有token时效
            StpUtil.logoutByTokenValue(userId);
        }
        Set<String> userOrgs = userOrgMapper.listAllOrg(userId);
        Set<String> keys = new HashSet<>();
        //强制移除权限缓存
        for (String orgId : userOrgs) {
            keys.add(GatewayConstants.USER_PERM_CACHE_PREFIX.formatted(userId, orgId));
        }
        stringRedisTemplate.delete(keys);
        //注销token的逻辑在网关中实现，因只有网关层可以看到token
    }

    private String getCaptchaCode(String key) {
        return stringRedisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + key);
    }

    private void deleteCaptcha(String key) {
        stringRedisTemplate.delete(CAPTCHA_KEY_PREFIX + key);
    }

    public CaptchaDTO getCaptcha() {
        Captcha captcha = new ArithmeticCaptcha(130, 48);
        String verCode = captcha.text();
        String key = UlidCreator.getUlid().toString();
        // 存入redis并设置过期时间为2分钟
        stringRedisTemplate.opsForValue().set(CAPTCHA_KEY_PREFIX + key, verCode, 2, TimeUnit.MINUTES);
        // 将key和base64返回给前端
        return new CaptchaDTO(key, captcha.toBase64());
    }
}
