package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 姚泰然
 * @date 2026-01-20 09:46
 */
@Data
@Accessors(chain = true)
public class OaAccessDataDTO {
    /**
     * 注册到oa的appid
     */
    private String appid;
    /**
     * oa系统里的用户id
     */
    private String userid;
    /**
     * 访问oa系统的token
     */
    private String token;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uri;
}
