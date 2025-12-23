package com.hbcy.authcenter.modules.core.tenant.vo;

import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 姚泰然
 * @date 2025-12-23 15:42
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TenantQueryVO extends PageVO {
    /**
     * 租户名称，模糊查询
     */
    private String nameCn;
    /**
     * 租户状态，0-正常，1-禁用
     */
    private Integer forbidden;
}
