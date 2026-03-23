package com.hbcy.authcenter.api.common.bean;

import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.common.base.error.ServerError;
import com.hbcy.common.web.api.NamedId;
import com.hbcy.common.web.bean.INameFillService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private Map<String, String> doQuery(Set<String> ids, String cacheKey,
                                        Function<Set<String>, List<NamedId>> queryFunc) {
        //使用kv存储，在redis集群中性能更好
        List<String> keys = new ArrayList<>();
        List<String> filteredIds = new ArrayList<>();
        for (String id : ids) {
            if (StringUtils.isNoneBlank(id) && !"0".equals(id)) {
                filteredIds.add(id);
                keys.add(cacheKey + id);
            }
        }
        List<String> r = stringRedisTemplate.opsForValue().multiGet(keys);
        if (r == null) r = Collections.emptyList();
        Set<String> missed = new HashSet<>();
        Map<String, String> result = new HashMap<>();
        result.put("0", "系统");
        result.put("", "系统");
        for (int i = 0; i < r.size(); i++) {
            if (r.get(i) == null) {
                missed.add(filteredIds.get(i));
            } else {
                result.put(filteredIds.get(i), r.get(i));
            }
        }
        if (!missed.isEmpty()) {
            List<NamedId> nameIds = queryFunc.apply(missed);
            Map<String, String> toSet = new HashMap<>();
            for (NamedId id : nameIds) {
                toSet.put(cacheKey + id.getItemId(), id.getItemName());
                result.put(id.getItemId(), id.getItemName());
            }
            if (!toSet.isEmpty()) {
                stringRedisTemplate.opsForValue().multiSet(toSet);
            }
        }
        return result;
    }

    public Map<String, String> getUserNameMap(Set<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        return doQuery(userIds, G.USER_NAME_CACHE_KEY,
                k -> userMapper.selectNameByIds(k));
    }

    public String getUserName(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return getUserNameMap(Set.of(userId)).get(userId);
    }

    @Override
    public Map<String, Map<String, String>> fillName(Map<String, Set<String>> queryMap) {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : queryMap.entrySet()) {
            if (entry.getKey().equals("userId")) {
                result.put(entry.getKey(), getUserNameMap(entry.getValue()));
            } else {
                throw new ServerError("unknown query key:%s", entry.getKey());
            }
        }
        return result;
    }
}
