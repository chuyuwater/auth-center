package com.hbcy.authcenter.api.common.bean;

import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.modules.core.org.service.OrgTreeService;
import com.hbcy.authcenter.api.modules.core.user.service.UserService;
import com.hbcy.common.base.error.ServerError;
import com.hbcy.common.web.api.NamedId;
import com.hbcy.common.web.bean.INameFillService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

/**
 * 缓存id对应的名字，前端显示友好
 * 因数据库通用字段create_user, update_user, create_org等需要频繁查询
 *
 * @author 姚泰然
 * @date 2025-12-26 16:36
 */
@Component
public class NameCacheService implements INameFillService {
    @Resource
    private UserService userService;

    @Resource
    private OrgTreeService orgTreeService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private Map<String, String> doQuery(Set<String> ids, String cacheKey,
                                        Function<Set<String>, List<NamedId>> queryFunc) {
        List<Object> uids = new ArrayList<>(ids);
        List<Object> r = stringRedisTemplate.opsForHash().multiGet(cacheKey, uids);
        Set<String> missed = new HashSet<>();
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < r.size(); i++) {
            if (r.get(i) == null) {
                missed.add(uids.get(i).toString());
            } else {
                result.put(uids.get(i).toString(), r.get(i).toString());
            }
        }
        if (!missed.isEmpty()) {
            List<NamedId> nameIds = queryFunc.apply(missed);
            Map<String, String> toPut = new HashMap<>();
            for (NamedId id : nameIds) {
                toPut.put(id.getItemId(), id.getItemName());
                result.put(id.getItemId(), id.getItemName());
            }
            if (!toPut.isEmpty()) {
                stringRedisTemplate.opsForHash().putAll(cacheKey, toPut);
            }
        }
        return result;
    }

    public Map<String, String> getUserNameMap(Set<String> userIds) {
        return doQuery(userIds, G.USER_NAME_CACHE_KEY,
                k -> userService.getBaseMapper().selectNameByIds(k));
    }

    public String getUserName(String userId) {
        return getUserNameMap(Set.of(userId)).get(userId);
    }

    public Map<String, String> getOrgNameMap(Set<String> orgIds) {
        return doQuery(orgIds, G.ORG_NAME_CACHE_KEY,
                k -> orgTreeService.getBaseMapper().selectNameByIds(k));
    }

    public String getOrgName(String orgId) {
        return getOrgNameMap(Set.of(orgId)).get(orgId);
    }

    @Override
    public Map<String, Map<String, String>> fillName(Map<String, Set<String>> queryMap) {
        Map<String, Map<String, String>> result = new HashMap<>();
        //TODO: 并行化加速
        for (Map.Entry<String, Set<String>> entry : queryMap.entrySet()) {
            if (entry.getKey().equals("userId")) {
                result.put(entry.getKey(), getUserNameMap(entry.getValue()));
            } else if (entry.getKey().equals("orgId")) {
                result.put(entry.getKey(), getOrgNameMap(entry.getValue()));
            } else {
                throw new ServerError("unknown query key:%s", entry.getKey());
            }
        }
        return result;
    }
}
