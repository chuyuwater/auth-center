package com.hbcy.authcenter.api.modules.intgr.oa.vo;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-01-22 11:35
 */
@Data
public class OaAuthorizeVO {
    /**
     * 配置在泛微后端配置文件ip白名单对应的应用id
     */
    private String clientid;
    /**
     * oa用户的手机号
     */
    private String loginid;
}
