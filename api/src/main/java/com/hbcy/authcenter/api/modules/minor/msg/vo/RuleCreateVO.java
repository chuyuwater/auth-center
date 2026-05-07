package com.hbcy.authcenter.api.modules.minor.msg.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RuleCreateVO {
    @NotBlank @Size(max = 20)
    private String ruleName;
    @NotBlank
    private String bizEntity;
    @NotBlank
    private String groupId;
    @Size(max = 200)
    private String description;
    private List<RuleConditionVO> conditions;
}
