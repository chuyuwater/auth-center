package com.hbcy.authcenter.api.common.pojo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 移动节点通用VO
 *
 * @author 姚泰然
 * @date 2025-12-25 09:01
 */
@Data
public class NodeMoveVO {
    /**
     * 被移动的节点
     */
    @NotBlank(message = "节点ID不能为空")
    private String nodeId;
    /**
     * 移动之后的父节点，为空标识根节点
     */
    private String parentId = "";
    /**
     * 移动之后的前一个节点，为空标识置顶
     */
    private String prevId = "";
}
