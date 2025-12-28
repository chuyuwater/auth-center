package com.hbcy.authcenter.api.modules.core.app.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2025-12-28 14:16
 */
@Data
public class ResPermDTO {
    private String id;
    private String appId;
    private String resId;
    private String permName;
    private String permCode;
    private Integer apiMethod;
    private String apiPath;
}
