package com.hbcy.authcenter.api.modules.core.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.core.auth.dao.TenantAccessMapper;
import com.hbcy.authcenter.api.modules.core.auth.model.TenantAccess;
import com.hbcy.authcenter.api.modules.core.auth.vo.TenantAccessUpsertVO;
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
 * AK/SK是租户级别的，其权限等于租户管理员，普通用户无法使用
 *
 * @author 姚泰然
 * @date 2026-01-26 16:56
 */
@Service
public class TenantAccessService {
    public static final Duration EXPIRE_TIME = Duration.ofMinutes(30);
    @Resource
    private TenantAccessMapper tenantAccessMapper;
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
    public TenantAccess checkAccess(String ak) {
        TenantAccess inst = tenantAccessMapper.selectOne(new QueryWrapper<TenantAccess>()
                .eq(TenantAccess.COL_ACCESS_KEY, ak)
                .eq(TenantAccess.COL_FORBIDDEN, 0));
        TenantAccess resp = inst;
        if (inst != null) {
            if (inst.getExpireTime() != null && inst.getExpireTime().isBefore(LocalDateTime.now())) {
                resp = null;
            }
        }
        stringRedisTemplate.opsForValue().set(
                GatewayConstants.USER_ACCESS_KEY_PREFIX + ak, JsonUtils.toJsonStr(resp), EXPIRE_TIME);
        return resp;
    }

    public TenantAccess createAccess(TenantAccessUpsertVO vo) {
        TenantAccess tenantAccess = new TenantAccess();
        tenantAccess.setKeyName(vo.getKeyName());
        tenantAccess.setAccessKey(UlidCreator.getUlid().toString());
        tenantAccess.setSecretKey(generateSK());
        tenantAccess.setExpireTime(vo.getExpireTime());
        tenantAccess.setTenantId(UserContextUtils.getTenantId());
        tenantAccess.setForbidden(vo.getForbidden());
        tenantAccessMapper.insert(tenantAccess);
        stringRedisTemplate.delete(GatewayConstants.USER_ACCESS_KEY_PREFIX + tenantAccess.getAccessKey());
        return tenantAccess;
    }

    public TenantAccess updateAccess(String id, TenantAccessUpsertVO vo) {
        TenantAccess ua = tenantAccessMapper.selectById(id);
        BeanCopyUtils.copy(vo, ua);
        tenantAccessMapper.updateById(ua);
        stringRedisTemplate.delete(GatewayConstants.USER_ACCESS_KEY_PREFIX + ua.getAccessKey());
        return ua;
    }

    public void deleteAccess(String id) {
        TenantAccess tenantAccess = tenantAccessMapper.selectById(id);
        if (tenantAccess == null) {
            return;
        }
        if (!tenantAccess.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        stringRedisTemplate.delete(GatewayConstants.USER_ACCESS_KEY_PREFIX + tenantAccess.getAccessKey());
        tenantAccessMapper.deleteById(id);
    }

    public TenantAccess getById(String id) {
        TenantAccess ua = tenantAccessMapper.selectById(id);
        if (ua == null) {
            throw new ParamError("指定id不存在");
        }
        if (!ua.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        return ua;
    }

    public List<TenantAccess> getTenantAKs() {
        return tenantAccessMapper.selectList(new QueryWrapper<TenantAccess>()
                .eq(TenantAccess.COL_TENANT_ID, UserContextUtils.getTenantId()));
    }
}
