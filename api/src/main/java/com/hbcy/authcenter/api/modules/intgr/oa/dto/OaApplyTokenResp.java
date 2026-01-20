package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-01-19 18:01
 */
@Data
public class OaApplyTokenResp {
    private String msg;
    private int code;
    private String token;
}
