package com.hbcy.authcenter.api.modules.minor.msg.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增渠道请求体
 */
@Data
public class ChannelCreateVO {
    @NotBlank @Size(max = 20)
    private String channelName;
    @NotBlank
    private String channelType;   // wechat/sms/dingtalk/site_msg
    @NotBlank
    private String provider;      // 服务商编码
    private String configJson;    // 渠道配置参数JSON
    @Size(max = 200)
    private String description;
}
