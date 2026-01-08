package com.hbcy.authcenter.gateway.dto;

import lombok.Data;

/**
 * API权限映射信息
 *
 * @author 姚泰然
 * @date 2025-12-31 12:15
 */
@Data
public class ApiPermDTO {
    /**
     * 0-GET, 1-POST, 2-PUT, 3-DELETE
     */
    private Integer apiMethod;
    /**
     * API路径
     */
    private String apiPath;
    /**
     * 权限id
     */
    private String permId;
    /**
     * 应用id
     */
    private String appId;
}
