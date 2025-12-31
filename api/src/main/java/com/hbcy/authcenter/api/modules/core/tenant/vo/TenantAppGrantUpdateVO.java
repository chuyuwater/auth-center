package com.hbcy.authcenter.api.modules.core.tenant.vo;

import lombok.Data;

import java.util.Set;

/**
 * 租户应用授权更新VO
 *
 * @author 姚泰然
 * @date 2025-12-28 17:45
 */
@Data
public class TenantAppGrantUpdateVO {
    /**
     * 授权的权限范围
     */
    Set<String> permIds;
    /**
     * 是否授权全部权限
     */
    private boolean grantAll;
}
