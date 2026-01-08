package com.hbcy.authcenter.gateway.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 姚泰然
 * @date 2026-01-08 08:44
 */
@Data
@Accessors(chain = true)
public class SessionDTO {
    private String userId;
    private String tenantId;
}
