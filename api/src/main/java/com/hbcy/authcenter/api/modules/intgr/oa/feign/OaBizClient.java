package com.hbcy.authcenter.api.modules.intgr.oa.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * OA业务接口
 * @author 姚泰然
 * @date 2026-01-20 14:49
 */
@FeignClient(name = "oa-biz", url = "${oa.url:}", configuration = OaReqConfig.class)
public interface OaBizClient {

}
