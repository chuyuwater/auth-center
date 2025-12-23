package com.hbcy.authcenter.api.modules.core.app.vo;

import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

/**
 * @author 姚泰然
 * @date 2025-12-23 16:26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryVO extends PageVO {
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
