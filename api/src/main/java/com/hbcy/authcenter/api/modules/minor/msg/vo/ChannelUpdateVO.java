package com.hbcy.authcenter.api.modules.minor.msg.vo;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改渠道请求体
 */
@Data
public class ChannelUpdateVO {
    @Size(max = 20)
    private String channelName;
    private String configJson;
    @Size(max = 200)
    private String description;
}
