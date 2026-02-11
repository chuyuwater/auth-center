package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-02-10 16:38
 */
@Data
public class OaWorkflowResp<T> {
    public static final String SUCCESS = "SUCCESS";
    private String code;
    private T data;

    public boolean isSuccess() {
        return SUCCESS.equals(code);
    }
}
