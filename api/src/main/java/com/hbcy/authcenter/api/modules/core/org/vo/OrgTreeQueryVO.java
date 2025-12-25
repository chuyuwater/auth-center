package com.hbcy.authcenter.api.modules.core.org.vo;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2025-12-25
 */
@Data
public class OrgTreeQueryVO {
    /**
     * 父节点ID，若为空则查询整个树
     */
    private String parentId;
    
}
