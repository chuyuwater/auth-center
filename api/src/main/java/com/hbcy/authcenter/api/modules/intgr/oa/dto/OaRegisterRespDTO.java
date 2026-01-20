package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-01-19 17:59
 */
@Data
public class OaRegisterRespDTO {
    private String msg;
    private int code;
    private String secret;
    private String status;
    private String spk;
}
