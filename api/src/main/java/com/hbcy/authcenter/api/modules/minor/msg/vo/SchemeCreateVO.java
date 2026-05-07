package com.hbcy.authcenter.api.modules.minor.msg.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SchemeCreateVO {
    @NotBlank @Size(max = 20)
    private String schemeName;
    @NotBlank
    private String templateId;
    @NotBlank
    private String groupId;
    private int receiverType;
    private String ruleId;
    private boolean retryEnabled = true;
    private int retryInterval = 20;
    private int retryMaxCount = 5;
    @Size(max = 200)
    private String description;
    @NotEmpty
    private List<String> channelIds;
}
