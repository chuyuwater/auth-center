package com.hbcy.authcenter.api.modules.core.auth.controller;

import com.hbcy.authcenter.api.modules.core.auth.model.UserAccess;
import com.hbcy.authcenter.api.modules.core.auth.service.UserAccessService;
import com.hbcy.authcenter.api.modules.core.auth.vo.UserAccessUpsertVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-26 19:30
 */
@RestController
@RequestMapping("/api/portal/v1/user/access-token")
public class UserAccessController {
    @Resource
    private UserAccessService userAccessService;

    @PostMapping
    public UserAccess createUserAccess(@Valid @RequestBody UserAccessUpsertVO vo) {
        return userAccessService.createUserAccess(vo);
    }

    @PutMapping("/{id}")
    public UserAccess updateUserAccess(@PathVariable String id, @Valid @RequestBody UserAccessUpsertVO vo) {
        return userAccessService.updateUserAccess(id, vo);
    }

    @DeleteMapping("/{id}")
    public void deleteUserAccess(@PathVariable String id) {
        userAccessService.deleteUserAccess(id);
    }

    @GetMapping("/{id}")
    public UserAccess getUserAccess(@PathVariable String id) {
        return userAccessService.getById(id);
    }

    @GetMapping
    public List<UserAccess> listUserAccess() {
        return userAccessService.getUserAKs();
    }
}
