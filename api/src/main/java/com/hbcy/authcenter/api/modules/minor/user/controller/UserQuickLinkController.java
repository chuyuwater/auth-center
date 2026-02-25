package com.hbcy.authcenter.api.modules.minor.user.controller;

import com.hbcy.authcenter.api.modules.minor.user.model.UserQuickLink;
import com.hbcy.authcenter.api.modules.minor.user.service.UserQuickLinkService;
import com.hbcy.authcenter.api.modules.minor.user.vo.UserQuickLinkUpsertVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 快捷入口
 * @author 姚泰然
 * @module open
 * @date 2026-02-25 17:29
 */
@RestController
@Validated
@RequestMapping("api/portal/v1/self/quick-link")
public class UserQuickLinkController {

    @Resource
    private UserQuickLinkService userQuickLinkService;

    /**
     * 覆盖用户快捷入口
     */
    @PostMapping
    public void overwriteQuickLink(@Valid @RequestBody UserQuickLinkUpsertVO vo) {
        userQuickLinkService.overwrite(vo);
    }

    /**
     * 获取用户快捷入口
     * @param clientType 客户端类型 1-PC, 2-移动端
     * @return 快捷入口列表
     */
    @GetMapping
    public List<UserQuickLink> listQuickLink(@NotNull(message = "clientType必须指定") Integer clientType) {
        return userQuickLinkService.list(clientType);
    }
}
