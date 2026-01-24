package com.hbcy.authcenter.api.modules.sys.dict.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hbcy.authcenter.api.modules.sys.dict.model.SysDict;
import com.hbcy.common.db.convertor.IDictService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * db中的枚举转系统字典适配器
 *
 * @author 姚泰然
 * @date 2025-12-23 14:59
 */
@Service
public class DictEnumAdapter implements IDictService {
    //在一次长列表页中返回，需要内存缓存避免N+1查询
    private final Cache<String, Map<String, String>> dictCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(10))
            .build();
    @Resource
    private SysDictService sysDictService;

    @Override
    public boolean isValidValue(String dictCode, String value) {
        return sysDictService.checkExist(dictCode, value) != null;
    }

    @Override
    public Map<String, String> getCodeEnums(String dictCode) {
        Map<String, String> cached = dictCache.getIfPresent(dictCode);
        if (cached != null) {
            return cached;
        }
        List<SysDict> list = sysDictService.listDictByFeatCode(dictCode, "");
        Map<String, String> resp = list.stream().collect(
                Collectors.toMap(SysDict::getValueStr, SysDict::getValueCn));
        dictCache.put(dictCode, resp);
        return resp;
    }
}
