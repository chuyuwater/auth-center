package com.hbcy.authcenter.api.modules.minor.msg;

import com.hbcy.authcenter.api.modules.minor.msg.engine.ChannelSender;
import com.hbcy.authcenter.api.modules.minor.msg.engine.ChannelSenderFactory;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendContext;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendResult;
import com.hbcy.authcenter.api.modules.minor.msg.engine.sender.SiteMsgSender;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 渠道发送工厂测试
 */
class ChannelSenderFactoryTest {

    @Test
    void getSender_existingSender_returnsCorrectSender() {
        SiteMsgSender siteMsgSender = new SiteMsgSender();
        ChannelSenderFactory factory = new ChannelSenderFactory(List.of(siteMsgSender));

        ChannelSender sender = factory.getSender("site_msg", "none");
        assertNotNull(sender);
        assertInstanceOf(SiteMsgSender.class, sender);
    }

    @Test
    void getSender_nonExistingSender_returnsNull() {
        SiteMsgSender siteMsgSender = new SiteMsgSender();
        ChannelSenderFactory factory = new ChannelSenderFactory(List.of(siteMsgSender));

        ChannelSender sender = factory.getSender("sms", "aliyun");
        assertNull(sender);
    }

    @Test
    void siteMsgSender_isSiteMsg_returnsTrue() {
        SiteMsgSender sender = new SiteMsgSender();
        assertTrue(sender.isSiteMsg());
        assertEquals("site_msg", sender.getChannelType());
        assertEquals("none", sender.getProvider());
    }

    @Test
    void siteMsgSender_send_returnsSuccess() {
        SiteMsgSender sender = new SiteMsgSender();
        MsgSendContext ctx = new MsgSendContext()
                .setTargetUserId("user1")
                .setMsgTitle("测试消息");
        MsgSendResult result = sender.send(ctx);
        assertTrue(result.isSuccess());
    }
}
