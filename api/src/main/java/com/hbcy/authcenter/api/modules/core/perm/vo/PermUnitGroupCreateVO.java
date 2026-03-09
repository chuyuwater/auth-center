package com.hbcy.authcenter.api.modules.core.perm.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限单元分组创建VO
 *
 * @author 姚泰然
 * @date 2025-12-28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PermUnitGroupCreateVO extends PermUnitGroupUpdateVO {
    /**
     * 策略模型
     * 0-RBAC, 1-ABAC
     */
    private Integer policyModel = 0;
}
