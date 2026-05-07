package com.hbcy.authcenter.api.modules.minor.msg.engine.sender;

import com.hbcy.authcenter.api.modules.minor.msg.engine.ChannelSender;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendContext;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信发送器
 * 需引入 com.aliyun:dysmsapi20170525 依赖
 * 当前为骨架实现，引入SDK后完善
 */
@Component
@Slf4j
public class AliyunSmsSender implements ChannelSender {
    @Override
    public String getChannelType() {
        return "sms";
    }

    @Override
    public String getProvider() {
        return "aliyun";
    }

    @Override
    public MsgSendResult send(MsgSendContext context) {
        // TODO: 引入阿里云SDK后完善实现
        // AliyunSmsConfig config = JsonUtils.readValue(context.getConfigJson(), AliyunSmsConfig.class);
        // Client client = createClient(config);
        // SendSmsRequest request = new SendSmsRequest()
        //     .setPhoneNumbers(context.getTargetUserPhone())
        //     .setSignName(config.getSignName())
        //     .setTemplateCode(context.getThirdTemplateId())
        //     .setTemplateParam(JsonUtils.toJsonStr(context.getVariables()));
        // SendSmsResponse response = client.sendSms(request);
        log.warn("阿里云短信发送器尚未接入SDK，跳过发送: phone={}", context.getTargetUserPhone());
        return MsgSendResult.fail("阿里云短信SDK未引入");
    }
}
