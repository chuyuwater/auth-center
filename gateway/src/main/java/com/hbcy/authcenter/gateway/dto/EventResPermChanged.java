package com.hbcy.authcenter.gateway.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 资源权限变更
 *
 * @author 姚泰然
 * @date 2026-01-08 10:51
 */
@Data
@Accessors(chain = true)
public class EventResPermChanged {
    private String appId;
    private String resId;
}
