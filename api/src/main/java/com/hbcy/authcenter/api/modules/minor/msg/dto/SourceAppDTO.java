package com.hbcy.authcenter.api.modules.minor.msg.dto;

import lombok.Data;

/**
 * 消息来源应用项（已推送消息的应用）
 *
 * @author 姚泰然
 * @date 2026-03-11
 */
@Data
public class SourceAppDTO {
    /**
     * 源应用id
     */
    private String srcApp;
    /**
     * 源应用名称
     */
    private String srcAppName;
}
