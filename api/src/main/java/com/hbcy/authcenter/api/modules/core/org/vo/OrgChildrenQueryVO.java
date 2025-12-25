package com.hbcy.authcenter.api.modules.core.org.vo;

import com.hbcy.authcenter.api.common.enums.OrgNodeTypeEnum;
import lombok.Data;

/**
 * @author 姚泰然
 * @date 2025-12-25 16:07
 */
@Data
public class OrgChildrenQueryVO {
    /**
     * 父节点
     */
    private String parentId = "";
    /**
     * 过滤节点类型
     */
    private OrgNodeTypeEnum nodeType;
}
