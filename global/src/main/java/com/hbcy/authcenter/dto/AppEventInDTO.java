package com.hbcy.authcenter.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * 应用事件，接收时用
 *
 * @author 姚泰然
 * @date 2025-12-31 14:19
 */
@Data
public class AppEventInDTO {
    /**
     * 应用id
     */
    private String appId = "portal";
    /**
     * 事件编码
     */
    private String code;
    /**
     * 事件详情
     * 可以用treeToValue转成具体的数据结构
     * 也可以直接取值
     */
    private JsonNode info;
}
