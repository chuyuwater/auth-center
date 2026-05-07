package com.hbcy.authcenter.api.modules.minor.msg.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RuleConditionVO {
    @NotBlank
    private String condType;    // org/user/role/perm
    private String condOp = "in"; // in/not_in
    @NotBlank
    private List<String> condValues;
    private Integer showOrder;
}
