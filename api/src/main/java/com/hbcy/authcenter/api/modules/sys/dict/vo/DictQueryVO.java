package com.hbcy.authcenter.modules.sys.dict.vo;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2025-12-22 14:48
 */
@Data
public class DictQueryVO {
    /**
     * 父节点Id（与其他条件二选一）
     */
    private String parentId;

    /**
     * 父节点字典键
     */
    private String featCode;
    /**
     * 父节点字典值
     */
    private String valueStr;
}
