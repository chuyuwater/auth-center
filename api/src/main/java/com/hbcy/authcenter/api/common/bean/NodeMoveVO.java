package com.hbcy.authcenter.api.common.bean;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.hbcy.common.base.error.ParamError;
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
    @JsonSetter(nulls = Nulls.SKIP)
    private String parentId = "";
    /**
     * 移动之后的前一个节点，为空标识置顶
     */
    @JsonSetter(nulls = Nulls.SKIP)
    private String prevId = "";

    public void check() {
        if (nodeId.equals(parentId) || nodeId.equals(prevId)) {
            throw new ParamError("参数错误");
        }
    }
}
