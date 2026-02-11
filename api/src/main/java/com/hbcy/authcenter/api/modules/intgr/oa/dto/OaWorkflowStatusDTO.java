package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-02-11 10:15
 */
@Data
public class OaWorkflowStatusDTO {
    //流程实例id
    private String requestId;
    private String requestLevel;
    private String requestName;
    //待办状态
    private String isremark;
    private WorkflowBaseInfo workflowBaseInfo;
    //流程id
    private String workflowId;

    @Data
    public static class WorkflowBaseInfo {
        //流程类型id
        private String workflowTypeId;
        private String workflowName;
    }
}
