package com.hbcy.authcenter.api.modules.sys.dict.service;

import com.hbcy.authcenter.api.modules.sys.dict.model.SysDict;
import com.hbcy.common.db.convertor.IDictService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
    @Resource
    private SysDictService sysDictService;

    @Override
    public boolean isValidValue(String dictCode, String value) {
        return sysDictService.checkExist(dictCode, value) != null;
    }

    @Override
    public Map<String, String> getCodeEnums(String dictCode) {
        List<SysDict> list = sysDictService.listDictByFeatCode(dictCode, "");
        return list.stream().collect(Collectors.toMap(SysDict::getValueStr, SysDict::getValueCn));
    }
}
