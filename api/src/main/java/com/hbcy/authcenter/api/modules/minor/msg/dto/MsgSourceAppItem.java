package com.hbcy.authcenter.api.modules.minor.msg.dto;

import lombok.Data;

/**
 * 消息来源应用项（已推送消息的应用）
 *
 * @author 姚泰然
 * @date 2026-03-11
 */
@Data
public class MsgSourceAppItem {
    private String srcApp;
    private String srcAppName;
}
