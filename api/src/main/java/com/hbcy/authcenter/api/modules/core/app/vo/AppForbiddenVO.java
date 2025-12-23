package com.hbcy.authcenter.api.modules.core.app.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * @author 姚泰然
 * @date 2025-12-23 16:25
 */
@Data
public class AppForbiddenVO {
    /**
     * 应用id
     */
    private String appId;
    /**
     * 是否禁用
     */
    @Range(min = 0, max = 1, message = "状态只能为0或1")
    private Integer forbidden;
}
