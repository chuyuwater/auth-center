package com.hbcy.authcenter.api.modules.minor.msg.engine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 渠道发送结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MsgSendResult {
    private boolean success;
    private String failReason;

    public static MsgSendResult ok() {
        return new MsgSendResult(true, null);
    }

    public static MsgSendResult fail(String reason) {
        return new MsgSendResult(false, reason);
    }
}
