package com.hbcy.authcenter.api.modules.minor.msg;

import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 发送结果测试
 */
class MsgSendResultTest {

    @Test
    void ok_returnsSuccessResult() {
        MsgSendResult result = MsgSendResult.ok();
        assertTrue(result.isSuccess());
        assertNull(result.getFailReason());
    }

    @Test
    void fail_returnsFailResult() {
        MsgSendResult result = MsgSendResult.fail("限流");
        assertFalse(result.isSuccess());
        assertEquals("限流", result.getFailReason());
    }
}
