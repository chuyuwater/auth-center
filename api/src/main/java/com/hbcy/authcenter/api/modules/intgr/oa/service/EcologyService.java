package com.hbcy.authcenter.api.modules.intgr.oa.service;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.intgr.oa.dto.OaAccessHeaders;
import com.hbcy.authcenter.api.modules.intgr.oa.dto.OaApplyTokenResp;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.error.ServerError;
import com.hbcy.common.lock.service.RedissonDistributedLock;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author 姚泰然
 * @date 2026-01-19 17:33
 */
@Service
public class EcologyService {
    public static final String LOCK_KEY = "portal:authCenter:oa:token:lock";
    public static final String TOKEN_KEY = "portal:authCenter:oa:token";
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private EcologyClient ecologyClient;
    @Resource
    private RedissonDistributedLock redissonDistributedLock;
    @Resource
    private UserMapper userMapper;
    private RSA rsa;
    /**
     * OA系统返回的密钥
     */
    @Value("${oa.secret}")
    private String secret;
    /**
     * OA返回的公钥
     */
    @Value("${oa.public-key}")
    private String spk;
    /**
     * 注册在oa里面的appId
     */
    @Value("${oa.app-id:ChuyuPortalDev}")
    private String appId;

    @PostConstruct
    public void init() {
        rsa = new RSA(null, spk);
    }

    public OaAccessHeaders getOaAccessHeaders() {
        String uid = UserContextUtils.getUserId();
        User user = userMapper.selectById(uid);
        if (user == null || user.getForbidden() == 1) {
            throw new PermissionError();
        }
        if (StringUtils.isBlank(user.getSrcId())) {
            throw new ParamError("用户未绑定oa账号");
        }
        return getOaAccessHeaders(user.getSrcId());
    }

    /**
     * 获取OA访问header，相当于服务端替用户完成了登录
     * @param userId oa中的用户id，不是手机号
     * @return 跳转链接
     */
    public OaAccessHeaders getOaAccessHeaders(String userId) {
        String token = stringRedisTemplate.opsForValue().get(TOKEN_KEY);
        if (StringUtils.isBlank(token)) {
            if (redissonDistributedLock.tryLock(LOCK_KEY, TimeUnit.SECONDS, 30, 60)) {
                try {
                    String encryptSecret = rsa.encryptBase64(secret, CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
                    OaApplyTokenResp resp = ecologyClient.applyToken(appId, encryptSecret, "3600");
                    if (resp.getCode() == 0) {
                        token = resp.getToken();
                        stringRedisTemplate.opsForValue().set(TOKEN_KEY, token, 3600, TimeUnit.SECONDS);
                    }
                } finally {
                    redissonDistributedLock.unlock(LOCK_KEY);
                }
            } else {
                token = stringRedisTemplate.opsForValue().get(TOKEN_KEY);
            }
        }
        if (StringUtils.isBlank(token)) {
            throw new ServerError("服务通信错误，请重试");
        }
        String uid = rsa.encryptBase64(userId, CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
        return new OaAccessHeaders(appId, uid, token);
    }
}
