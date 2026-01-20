package com.hbcy.authcenter.api.modules.intgr.oa.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * OA认证接口
 * @author 姚泰然
 * @date 2026-01-19 13:51
 */
@FeignClient(name = "oa-auth", url = "${oa.url:}")
public interface OaAuthClient {
    /** 获取 Token */
    @PostMapping(value = "/api/ec/dev/auth/applytoken", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String applyToken(@RequestHeader("appid") String appid,
                      @RequestHeader("secret") String encryptedSecret,
                      @RequestHeader("time") String time);
}
