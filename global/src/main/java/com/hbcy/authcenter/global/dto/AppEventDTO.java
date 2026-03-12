package com.hbcy.authcenter.global.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.hbcy.common.base.json.JsonUtils;
import lombok.Data;

/**
 * 应用事件，接收时用
 *
 * @author 姚泰然
 * @date 2025-12-31 14:19
 */
@Data
public class AppEventDTO {

    /**
     * 事件元数据
     */
    private EventMeta meta;
    /**
     * 事件详情
     * 可以用treeToValue转成具体的数据结构
     * 也可以直接取值
     */
    private JsonNode payload;

    public void fillPayload(Object data) {
        this.payload = JsonUtils.value2tree(data);
    }
}
