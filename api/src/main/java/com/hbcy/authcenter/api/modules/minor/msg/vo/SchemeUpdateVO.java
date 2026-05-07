package com.hbcy.authcenter.api.modules.minor.msg.vo;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SchemeUpdateVO {
    @Size(max = 20)
    private String schemeName;
    private String groupId;
    private Integer receiverType;
    private String ruleId;
    private Boolean retryEnabled;
    private Integer retryInterval;
    private Integer retryMaxCount;
    @Size(max = 200)
    private String description;
    private List<String> channelIds;
}
