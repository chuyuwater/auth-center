package com.hbcy.authcenter.api.modules.core.perm.vo;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

/**
 * 权限单元分组查询VO
 *
 * @author 姚泰然
 * @date 2025-12-28
 */
@Data
public class PermUnitGroupQueryVO {
    /**
     * 父节点ID，若为空则查询整个树
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private String parentId = "";

    /**
     * 关键字
     */
    private String keyword;
}
