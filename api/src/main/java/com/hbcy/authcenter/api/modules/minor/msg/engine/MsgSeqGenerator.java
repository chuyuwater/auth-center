package com.hbcy.authcenter.api.modules.minor.msg.engine;

import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 消息编号生成器
 * 格式：{租户前缀}-{类型前缀}-{6位序号}
 */
@Service
public class MsgSeqGenerator {
    private static final String KEY_PREFIX = "portal:msg:seq:";
    public static final String TYPE_CHANNEL = "MessageCh";
    public static final String TYPE_TEMPLATE = "MessageTem";
    public static final String TYPE_SCHEME = "MessageSch";
    public static final String TYPE_RULE = "Rule";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成编号
     *
     * @param tenantId   租户id
     * @param typePrefix 类型前缀
     * @return 编号
     */
    public String generate(String tenantId, String typePrefix) {
        String key = KEY_PREFIX + tenantId + ":" + typePrefix;
        Long seq = stringRedisTemplate.opsForValue().increment(key);
        String tenantPrefix = resolveTenantPrefix(tenantId);
        return tenantPrefix + "-" + typePrefix + "-" + String.format("%06d", seq);
    }

    /**
     * 生成编号（使用当前用户租户）
     */
    public String generate(String typePrefix) {
        return generate(UserContextUtils.getTenantId(), typePrefix);
    }

    private String resolveTenantPrefix(String tenantId) {
        if (G.DEFAULT_TENANT.equals(tenantId)) {
            return "A";
        }
        // 简单映射：取租户id的首字母大写，若为数字则用T前缀
        char first = tenantId.charAt(0);
        if (Character.isDigit(first)) {
            return "T" + first;
        }
        return String.valueOf(Character.toUpperCase(first));
    }
}
