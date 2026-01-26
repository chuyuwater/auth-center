package com.hbcy.authcenter.api.modules.core.auth.controller;

import com.hbcy.authcenter.api.modules.core.auth.model.UserAccess;
import com.hbcy.authcenter.api.modules.core.auth.service.UserAccessService;
import com.hbcy.authcenter.api.modules.core.auth.vo.UserAccessUpsertVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AK/SK认证
 *
 * @author 姚泰然
 * @module auth
 * @date 2026-01-26 19:30
 */
@RestController
@RequestMapping("/api/portal/v1/user/access-token")
public class UserAccessController {
    @Resource
    private UserAccessService userAccessService;

    /**
     * 创建访问密钥
     * @param vo 信息
     * @return 密钥信息
     */
    @PostMapping
    public UserAccess createUserAccess(@Valid @RequestBody UserAccessUpsertVO vo) {
        return userAccessService.createUserAccess(vo);
    }

    /**
     * 更新访问密钥
     * @param id 密钥ID
     * @param vo 信息
     * @return 密钥信息
     */
    @PutMapping("/{id}")
    public UserAccess updateUserAccess(@PathVariable String id, @Valid @RequestBody UserAccessUpsertVO vo) {
        return userAccessService.updateUserAccess(id, vo);
    }

    /**
     * 删除访问密钥
     * @param id 密钥id
     */
    @DeleteMapping("/{id}")
    public void deleteUserAccess(@PathVariable String id) {
        userAccessService.deleteUserAccess(id);
    }

    /**
     * 密钥详情
     * @param id 密钥id
     * @return 密钥详情
     */
    @GetMapping("/{id}")
    public UserAccess getUserAccess(@PathVariable String id) {
        return userAccessService.getById(id);
    }

    /**
     * 获取用户的所有密钥
     * @return 密钥列表
     */
    @GetMapping
    public List<UserAccess> listUserAccess() {
        return userAccessService.getUserAKs();
    }
}
