package com.hbcy.authcenter.gateway.bean;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.hbcy.authcenter.gateway.constants.GatewayConstants;
import com.hbcy.authcenter.gateway.dto.SessionDTO;
import com.hbcy.authcenter.gateway.dto.UserAccessDTO;
import com.hbcy.common.base.error.ClientError;
import com.hbcy.common.base.json.JsonUtils;
import com.hbcy.common.redis.RedisExtendService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 基于Redis的会话服务
 *
 * @author 姚泰然
 * @date 2026-01-08 08:38
 */
@Service
public class UserSessionService {
    @Resource
    private RedisExtendService redisExtendService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 检查登陆状态并返回用户id和租户id
     *
     * @param token 前端传入的token
     * @return 如果已登录则返回用户id，未登录返回null
     */
    @Nullable
    public SessionDTO checkLogin(String token) {
        try {
            String loginId = (String) StpUtil.getLoginIdByToken(token);
            SaSession session = StpUtil.getSessionByLoginId(loginId);
            return new SessionDTO()
                    .setUserId(loginId)
                    .setTenantId((String) session.get(GatewayConstants.SESSION_TENANT_ID));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查用户当前身份是否拥有某个权限
     *
     * @param userId 用户id
     * @param orgId  组织id
     * @param permId 权限id
     * @return -1: key已经过期，需要调用接口刷新缓存；0：无权限， 1：有权限
     */
    public int checkPerm(String userId, String orgId, String permId) {
        String key = GatewayConstants.USER_PERM_CACHE_PREFIX.formatted(userId, orgId);
        Long memberOrNonexist = redisExtendService.isMemberOrNonexist(key, permId);
        if (memberOrNonexist == null) {
            return 0;
        } else if (memberOrNonexist > 0) {
            return 1;
        } else if (memberOrNonexist < 0) {
            return -1;
        }
        return 0;
    }

    /**
     * 确认权限缓存过期时间，返回剩余时间
     * 用于异步预刷新用户权限缓存，提高API响应速度
     *
     * @param userId 用户id
     * @param orgId  组织id
     * @return 剩余时间，0表示已过期
     */
    public long getPermTTL(String userId, String orgId) {
        String key = GatewayConstants.USER_PERM_CACHE_PREFIX.formatted(userId, orgId);
        Long expire = stringRedisTemplate.getExpire(key);
        if (expire == null || expire == -2) {
            return 0;
        } else if (expire > 0) {
            return expire;
        }
        //永久有效，需要手动删除
        return 9999L;
    }

    public UserAccessDTO getAccessByAK(String ak) {
        String s = stringRedisTemplate.opsForValue().get(GatewayConstants.USER_ACCESS_KEY_PREFIX + ak);
        if (StringUtils.isNotBlank(s)) {
            //防止缓存穿透，直接抛异常
            UserAccessDTO dto = JsonUtils.readValue(s, UserAccessDTO.class);
            if (dto == null) {
                throw new ClientError("用户密钥不存在");
            }
        }
        return null;
    }
}
