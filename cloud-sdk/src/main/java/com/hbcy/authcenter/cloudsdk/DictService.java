package com.hbcy.authcenter.cloudsdk;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hbcy.authcenter.sdk.feign.AuthCenterClient;
import com.hbcy.authcenter.sdk.feign.dto.SysDictDTO;
import com.hbcy.common.base.error.ServerError;
import com.hbcy.common.base.pojo.ApiResponse;
import com.hbcy.common.db.convertor.IDictService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 姚泰然
 * @date 2026-04-13 14:09
 */
@Service
@Slf4j
public class DictService implements IDictService {
    //在一次长列表页中返回，需要内存缓存避免N+1查询
    private final Cache<String, Map<String, String>> dictCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(10))
            .build();
    @Resource
    private AuthCenterClient authCenterClient;

    private Map<String, String> fetchDict(String dictCode) {
        ApiResponse<List<SysDictDTO>> resp = authCenterClient.listDictByFeatCode(dictCode, "");
        if (resp.getStatus() != 0) {
            log.error("fail to request portal-auth-center:{}", resp.getMsg());
            throw new ServerError("字典服务错误:%s", resp.getMsg());
        }
        if (CollectionUtils.isEmpty(resp.getData())) {
            log.warn("dictCode:{} is empty", dictCode);
            return Map.of();
        }
        Map<String, String> dict = resp.getData().stream().collect(
                Collectors.toMap(SysDictDTO::getValueStr, SysDictDTO::getValueCn));
        dictCache.put(dictCode, dict);
        return dict;
    }

    @Override
    public boolean isValidValue(String dictCode, String value) {
        Map<String, String> dict = getCodeEnums(dictCode);
        return dict.containsKey(value);
    }

    @Override
    public Map<String, String> getCodeEnums(String dictCode) {
        Map<String, String> cached = dictCache.getIfPresent(dictCode);
        if (cached != null) {
            return cached;
        }
        return fetchDict(dictCode);
    }
}