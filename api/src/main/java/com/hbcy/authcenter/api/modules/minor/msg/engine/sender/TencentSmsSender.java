package com.hbcy.authcenter.api.modules.minor.msg.engine.sender;

import com.hbcy.authcenter.api.modules.minor.msg.engine.ChannelSender;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendContext;
import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 腾讯云短信发送器
 * 需引入 com.tencentcloudapi:tencentcloud-sdk-java-sms 依赖
 * 当前为骨架实现，引入SDK后完善
 */
@Component
@Slf4j
public class TencentSmsSender implements ChannelSender {
    @Override
    public String getChannelType() {
        return "sms";
    }

    @Override
    public String getProvider() {
        return "tencent";
    }

    @Override
    public MsgSendResult send(MsgSendContext context) {
        // TODO: 引入腾讯云SDK后完善实现
        // TencentSmsConfig config = JsonUtils.readValue(context.getConfigJson(), TencentSmsConfig.class);
        // Credential cred = new Credential(config.getSecretId(), config.getSecretKey());
        // SmsClient client = new SmsClient(cred, "ap-guangzhou", clientProfile);
        // SendSmsRequest req = new SendSmsRequest();
        // req.setSmsSdkAppId(config.getSmsSdkAppId());
        // req.setSignName(config.getSignName());
        // req.setTemplateId(context.getThirdTemplateId());
        // req.setPhoneNumberSet(new String[]{"+86" + context.getTargetUserPhone()});
        // if (context.getVariables() != null && !context.getVariables().isEmpty()) {
        //     req.setTemplateParamSet(context.getVariables().values().toArray(new String[0]));
        // }
        // SendSmsResponse resp = client.SendSms(req);
        log.warn("腾讯云短信发送器尚未接入SDK，跳过发送: phone={}", context.getTargetUserPhone());
        return MsgSendResult.fail("腾讯云短信SDK未引入");
    }
}
