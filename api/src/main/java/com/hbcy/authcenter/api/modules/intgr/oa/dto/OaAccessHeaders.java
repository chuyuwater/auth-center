package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-01-20 09:46
 */
@Data
@AllArgsConstructor
public class OaAccessHeaders {
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
}
