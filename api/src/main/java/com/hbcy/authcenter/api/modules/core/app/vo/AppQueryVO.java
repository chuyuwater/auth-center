package com.hbcy.authcenter.api.modules.core.app.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * @author 姚泰然
 * @date 2025-12-23 16:26
 */
@Data
public class AppQueryVO {
    /**
     * 中文名，支持模糊查询
     */
    private String nameCn;
    /**
     * 是否禁用
     */
    @Range(min = 0, max = 1, message = "状态只能为0或1")
    private Integer forbidden;
}
