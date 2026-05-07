package com.hbcy.authcenter.api.modules.minor.msg.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 模板消息内容
 */
@Data
public class TemplateContentVO {
    @NotBlank @Size(max = 50)
    private String msgTitle;
    @NotBlank @Size(max = 255)
    private String msgContent;
    @NotBlank
    private String channelId;
    /**
     * 第三方平台模板ID（短信渠道必填）
     * 阿里云：TemplateCode，如 SMS_15305xxxx
     * 腾讯云：TemplateId
     * 非短信渠道可不填
     */
    private String thirdTemplateId;
    private Integer showOrder;
}
