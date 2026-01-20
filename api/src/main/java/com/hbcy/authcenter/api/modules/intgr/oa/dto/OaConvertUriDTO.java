package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-01-20 08:50
 */
@Data
public class OaConvertUriDTO {
    private String schema;
    private int code;
    private int port;
    private String msgShowType;
    private String ip;
    private String host;
    private String type;
    private String url;
    private boolean status;
}
