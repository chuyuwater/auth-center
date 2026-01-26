package com.hbcy.authcenter.api.modules.core.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.core.auth.dao.UserAccessMapper;
import com.hbcy.authcenter.api.modules.core.auth.model.UserAccess;
import com.hbcy.authcenter.api.modules.core.auth.vo.UserAccessUpsertVO;
import com.hbcy.authcenter.gateway.constants.GatewayConstants;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.json.JsonUtils;
import com.hbcy.common.base.util.BeanCopyUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * TODO: 目前用户密钥的权限等于用户的权限，未做细化授权
 *
 * @author 姚泰然
 * @date 2026-01-26 16:56
 */
@Service
public class UserAccessService {
    public static final Duration EXPIRE_TIME = Duration.ofMinutes(30);
    @Resource
    private UserAccessMapper userAccessMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static String generateSK() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        // 使用 URL 安全的 Base64 编码，去掉补位符
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 通过ak获取密钥详情，用于网关通信
     *
     * @param ak ak
     * @return 详情
     */
    public UserAccess checkUserAccess(String ak) {
        UserAccess inst = userAccessMapper.selectOne(new QueryWrapper<UserAccess>()
                .eq(UserAccess.COL_ACCESS_KEY, ak)
                .eq(UserAccess.COL_FORBIDDEN, 0));
        UserAccess resp = inst;
        if (inst != null) {
            if (inst.getExpireTime() != null && inst.getExpireTime().isBefore(LocalDateTime.now())) {
                resp = null;
            }
        }
        stringRedisTemplate.opsForValue().set(
                GatewayConstants.USER_ACCESS_KEY_PREFIX + ak, JsonUtils.toJsonStr(resp), EXPIRE_TIME);
        return resp;
    }

    public UserAccess createUserAccess(UserAccessUpsertVO vo) {
        UserAccess userAccess = new UserAccess();
        userAccess.setKeyName(vo.getKeyName());
        userAccess.setAccessKey(UlidCreator.getUlid().toString());
        userAccess.setSecretKey(generateSK());
        userAccess.setExpireTime(vo.getExpireTime());
        userAccess.setUserId(UserContextUtils.getUserId());
        userAccess.setTenantId(UserContextUtils.getTenantId());
        userAccess.setForbidden(vo.getForbidden());
        userAccessMapper.insert(userAccess);
        stringRedisTemplate.delete(GatewayConstants.USER_ACCESS_KEY_PREFIX + userAccess.getAccessKey());
        return userAccess;
    }

    public UserAccess updateUserAccess(String id, UserAccessUpsertVO vo) {
        UserAccess ua = userAccessMapper.selectById(id);
        BeanCopyUtils.copy(vo, ua);
        userAccessMapper.updateById(ua);
        stringRedisTemplate.delete(GatewayConstants.USER_ACCESS_KEY_PREFIX + ua.getAccessKey());
        return ua;
    }

    public void deleteUserAccess(String id) {
        String uid = UserContextUtils.getUserId();
        UserAccess userAccess = userAccessMapper.selectById(id);
        if (userAccess == null) {
            return;
        }
        if (!userAccess.getUserId().equals(uid)) {
            throw new PermissionError();
        }
        stringRedisTemplate.delete(GatewayConstants.USER_ACCESS_KEY_PREFIX + userAccess.getAccessKey());
        userAccessMapper.deleteById(id);
    }

    public UserAccess getById(String id) {
        UserAccess ua = userAccessMapper.selectById(id);
        if (ua == null) {
            throw new ParamError("指定id不存在");
        }
        if (!ua.getUserId().equals(UserContextUtils.getUserId())) {
            throw new PermissionError();
        }
        return ua;
    }

    public List<UserAccess> getUserAKs() {
        return userAccessMapper.selectList(new QueryWrapper<UserAccess>()
                .eq(UserAccess.COL_USER_ID, UserContextUtils.getUserId()));
    }
}
