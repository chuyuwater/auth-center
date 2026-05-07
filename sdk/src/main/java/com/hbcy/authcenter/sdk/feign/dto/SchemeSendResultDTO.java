package com.hbcy.authcenter.sdk.feign.dto;

import lombok.Data;

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
    public static class ChannelSendDetail {
        private String channelId;
        private String channelName;
        /** 1-成功，2-失败 */
        private int status;
        private String failReason;
    }
}
