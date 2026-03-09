package com.hbcy.authcenter.api.modules.core.org.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-01-28 12:38
 */
@Data
public class OrgSwitchStatusVO {
    /**
     * 节点ID
     */
    @NotBlank(message = "节点ID不能为空")
    private String nodeId;
    /**
     * 状态,0-启用，1-禁用
     */
    private Integer forbidden = 0;
}
