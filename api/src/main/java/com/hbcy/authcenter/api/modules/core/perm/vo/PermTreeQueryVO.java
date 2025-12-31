package com.hbcy.authcenter.api.modules.core.perm.vo;

import lombok.Data;

/**
 * 权限树查询VO
 *
 * @author 姚泰然
 * @date 2025-12-28
 */
@Data
public class PermTreeQueryVO {
    /**
     * 父节点ID，若为空则查询整个树
     */
    private String parentId = "";

    /**
     * 关键字
     */
    private String keyword;

}
