package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-02-11 09:15
 */
@Data
public class OaWorkflowDetailDTO {
    private String requestId;
    private String requestLevel;
    private String requestName;
    private OaWorkflowBasicDTO workflowBaseInfo;
}
