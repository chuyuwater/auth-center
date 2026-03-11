package com.hbcy.authcenter.gateway.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-01-26 17:53
 */
@Data
public class TenantAccessDTO {
    private String id;
    private String accessKey;
    private String secretKey;
    private String tenantId;
}
