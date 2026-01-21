package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import lombok.Data;

/**
 * OA待办数据
 * @author 姚泰然
 * @date 2026-01-21 11:23
 */
@Data
public class OaTodoDTO {
    private String agentorbyagentid;
    private String agenttype;
    private String cid;
    private String createTime;
    private String creatorDepartmentId;
    private String creatorDepartmentName;
    private String creatorId;
    private String creatorName;
    private String creatorSubcompanyId;
    private String creatorSubcompanyName;
    private String currentNodeId;
    private String currentNodeName;
    private String currentnodetype;
    private String isbereject;
    private String isprocessed;
    private String isremark;
    private String lastOperateTime;
    private String lastOperatorId;
    private String lastOperatorName;
    private String nodeid;
    private String operateTime;
    private String preisremark;
    private String receiveTime;
    private String requestId;
    private String requestLevel;
    private String requestName;
    private String requestmark;
    private String status;
    private String sysName;
    private String takisremark;
    private String userDepartmentId;
    private String userDepartmentName;
    private String userName;
    private String userSubcompanyId;
    private String userSubcompanyName;
    private String userid;
    private String usertype;
    private String viewtype;
    private OaWorkflowDTO workflowBaseInfo;
}
