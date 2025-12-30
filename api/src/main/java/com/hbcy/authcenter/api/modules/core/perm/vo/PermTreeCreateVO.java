package com.hbcy.authcenter.api.modules.core.perm.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 姚泰然
 * @date 2025-12-28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PermTreeCreateVO extends PermTreeUpdateVO {
    /**
     * 父节点。
     * 创建时，节点固定在同级末尾。
     */
    private String parentId = "";
    /**
     * 策略模型
     * 0-RBAC, 1-ABAC
     */
    private int policyModel = 0;
}
