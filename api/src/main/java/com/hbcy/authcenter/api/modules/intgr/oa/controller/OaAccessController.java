package com.hbcy.authcenter.api.modules.intgr.oa.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.hbcy.authcenter.api.modules.intgr.oa.dto.OaAccessDataDTO;
import com.hbcy.authcenter.api.modules.intgr.oa.service.EcologyService;
import com.hbcy.common.base.json.JsonUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * OA单点登录
 * @author 姚泰然
 * @module sys
 * @date 2026-01-20 14:02
 */
@RestController
@RequestMapping
@Validated
@Slf4j
public class OaAccessController {
    @Resource
    private EcologyService ecologyService;

    /**
     * 获取客户端访问oa链接时需要的header
     * @ignore
     * @return header信息
     */
    @GetMapping("api/portal/v1/oa/headers")
    public OaAccessDataDTO getOaAccessHeaders() {
        return ecologyService.getOaAccessHeaders();
    }

    /**
     * 将oa的待办id转为跳转链接
     * @return 使用ajax请求返回值中的uri，并将其他字段作为header，根据返回值跳转（iframe或打开新页面）
     */
    @GetMapping("api/portal/v1/oa/workflow")
    public String getTodoPage(@NotBlank(message = "requestId不能为空") String requestId) {
        return ecologyService.accessWorkflow(requestId);
    }

    /**
     * 同步组织信息，内部调用
     * @ignore
     * @param oaOrgId 楚禹公司oa orgId
     * @param tenantId 楚禹公司租户id
     */
    @PostMapping("inner/portal/oa/sync")
    public void syncOrg(String oaOrgId, String tenantId) {
        ecologyService.sync(oaOrgId, tenantId);
    }

    /**
     * 接收oa消息推送
     * 该功能应使用ak+白名单功能，开放给OA系统调用
     */
    @PostMapping("api/portal/v1/oa/msg")
    public void notifyMsg(@RequestBody JsonNode body) {
        log.info("==========receive oa msg push:{}", JsonUtils.toJsonStr(body));
    }
}
