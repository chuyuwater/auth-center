package com.hbcy.authcenter.api.modules.intgr.oa.controller;

import com.hbcy.authcenter.api.modules.intgr.oa.dto.OaAccessHeaders;
import com.hbcy.authcenter.api.modules.intgr.oa.service.EcologyService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OA单点登录
 * @author 姚泰然
 * @date 2026-01-20 14:02
 */
@RestController
@RequestMapping("api/portal/v1/oa")
public class OaAccessController {
    @Resource
    private EcologyService ecologyService;

    /**
     * 获取客户端访问oa链接时需要的header
     * @return header信息
     */
    @GetMapping("/headers")
    public OaAccessHeaders getOaAccessHeaders() {
        return ecologyService.getOaAccessHeaders();
    }
}
