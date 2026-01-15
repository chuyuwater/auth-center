package com.hbcy.authcenter.api.modules.sys.dict.vo;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-01-14 17:37
 */
@Data
public class DictFuzzyQueryVO {
    /**
     * 字典类型id
     */
    private String groupId;
    /**
     * 关键字
     */
    private String keyword;
}
