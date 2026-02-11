package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import lombok.Data;

/**
 * OA工作流数据
 * @author 姚泰然
 * @date 2026-01-21 12:05
 */
@Data
public class OaWorkflowBasicDTO {
    private String formId;
    private String workflowId;
    private String workflowName;
    private String workflowTypeId;
    private String workflowTypeName;
}
