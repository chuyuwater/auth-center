package com.hbcy.authcenter.api.modules.minor.msg.engine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 方案发送结果DTO
 */
@Data
public class SchemeSendResultDTO {
    private int totalCount;
    private int successCount;
    private int failCount;
    private List<ChannelSendDetail> details;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChannelSendDetail {
        private String channelId;
        private String channelName;
        /** 1-成功，2-失败 */
        private int status;
        private String failReason;
    }
}
