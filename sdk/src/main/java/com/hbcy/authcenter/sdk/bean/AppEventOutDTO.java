package com.hbcy.authcenter.sdk.bean;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 应用事件通知，发送时用
 *
 * @author 姚泰然
 * @date 2025-12-31 14:11
 */
@Data
@Accessors(chain = true)
public class AppEventOutDTO {
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
     */
    private Object info;
}
