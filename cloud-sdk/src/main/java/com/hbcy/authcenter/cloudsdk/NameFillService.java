package com.hbcy.authcenter.cloudsdk;

import com.hbcy.authcenter.sdk.feign.AuthCenterClient;
import com.hbcy.common.base.error.ServerError;
import com.hbcy.common.base.pojo.ApiResponse;
import com.hbcy.common.web.bean.INameFillService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author 姚泰然
 * @date 2026-04-13 14:10
 */
@Service
@Slf4j
public class NameFillService implements INameFillService {
    public static final String APP_NAME = "iot";
    //与统一门户的用户名缓存key一致，同时部署时可以复用缓存
    public static final String USER_NAME_CACHE_KEY = "portal:name:user:";
    @Resource
    private AuthCenterClient authCenterClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public Map<String, String> getUserNameMap(Set<String> userIds) {
        List<String> idList = new ArrayList<>(userIds);
        List<String> keys = idList.stream().map(id -> USER_NAME_CACHE_KEY + id).toList();
        List<String> r = stringRedisTemplate.opsForValue().multiGet(keys);
        if (r == null) r = Collections.emptyList();
        Set<String> missed = new HashSet<>();
        Map<String, String> result = new HashMap<>();
        result.put("0", "系统");
        result.put("", "系统");
        for (int i = 0; i < r.size(); i++) {
            if (r.get(i) == null) {
                missed.add(idList.get(i));
            } else {
                result.put(idList.get(i), r.get(i));
            }
        }
        try {
            ApiResponse<Map<String, String>> resp = authCenterClient.getUserNames(missed);
            if (resp.getStatus() != 0) {
                log.error("fail to request portal-auth-center:{}", resp.getMsg());
            } else if (!CollectionUtils.isEmpty(resp.getData())) {
                result.putAll(resp.getData());
            }
        } catch (Exception e) {
            log.error("fail to request portal-auth-center:", e);
        }
        //portal侧会刷新缓存，这里无需处理
        return result;
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
