package com.hbcy.authcenter.api.modules.core.app.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 授权app信息
 *
 * @author 姚泰然
 * @date 2025-12-30 09:01
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GrantAppDTO extends AppCardDTO {
    /**
     * 授权id
     */
    private String grantId;
    /**
     * 是否禁用, 0-启用，1-禁用
     */
    private Integer forbidden;
}
