package com.hbcy.authcenter.api.modules.minor.user.controller;


import com.hbcy.authcenter.api.modules.minor.user.model.UserCustomStyle;
import com.hbcy.authcenter.api.modules.minor.user.service.UserCustomStyleService;
import com.hbcy.authcenter.api.modules.minor.user.vo.UserCustomStyleCreateVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户自定义页面风格
 * @module open
 * @author 姚泰然
 * @date 2026-04-03 14:43
 */
@Validated
@RestController
@RequestMapping("api/portal/v1/user/custom-style")
public class UserCustomStyleController {

    @Resource
    private UserCustomStyleService userCustomStyleService;

    /**
     * 当前用户自定义风格列表
     *
     * @param item 自定义项编码，可选；传值时仅返回该自定义项的数据
     * @return 当前登录用户的自定义风格列表
     */
    @GetMapping
    public List<UserCustomStyle> list(String item) {
        return userCustomStyleService.list4User(item);
    }

    /**
     * 自定义项详情
     *
     * @param item 自定义项
     * @return 当前登录用户下对应的自定义风格详情，不存在时返回null
     */
    @GetMapping("/{item}")
    public UserCustomStyle getById(@NotBlank(message = "id不能为空") @PathVariable String item) {
        return userCustomStyleService.getItem(item);
    }

    /**
     * 创建或更新自定义项
     *
     * @param vo 创建参数，包含自定义项item和设置值setting
     * @return 创建后的自定义风格
     */
    @PostMapping
    public UserCustomStyle upsert(@Valid @RequestBody UserCustomStyleCreateVO vo) {
        return userCustomStyleService.upsert(vo);
    }

    /**
     * 删除自定义风格
     *
     * @param item 自定义项标识
     */
    @PostMapping("/delete")
    public void delete(@NotBlank(message = "item不能为空") String item) {
        userCustomStyleService.delete(item);
    }
}
