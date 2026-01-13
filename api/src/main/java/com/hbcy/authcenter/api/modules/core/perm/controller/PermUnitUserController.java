package com.hbcy.authcenter.api.modules.core.perm.controller;

import com.hbcy.authcenter.api.modules.core.perm.dto.UnitUserDTO;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitUserService;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserUpdateVO;
import com.hbcy.common.base.pojo.BatchDeleteVO;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.web.bean.NameFill;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色用户管理（租户侧）
 *
 * @author 姚泰然
 * @module perm
 * @date 2025-12-28
 */
@RestController
@RequestMapping("api/portal/v1/perm/unit/users")
@Validated
public class PermUnitUserController {

    @Resource
    private PermUnitUserService permUnitUserService;

    /**
     * 添加用户到权限单元
     *
     * @param vo 添加用户到单位请求参数
     */
    @PostMapping
    public void addUsersToUnit(@Valid @RequestBody PermUnitUserUpdateVO vo) {
        permUnitUserService.addUsersToUnit(vo);
    }

    /**
     * 按授权id移除授权
     *
     * @param grantId 授权ID
     */
    @DeleteMapping
    public void removeUsersFromUnit(@RequestParam String grantId) {
        permUnitUserService.deleteGrant(List.of(grantId));
    }

    /**
     * 按授权id批量移除授权
     *
     * @param vo 批量删除请求参数
     */
    @PostMapping("/batch-delete")
    public void batchDeleteUsers(@Valid @RequestBody BatchDeleteVO vo) {
        permUnitUserService.deleteGrant(vo.getIds());
    }

    /**
     * 授权用户列表
     *
     * @param vo 查询条件
     * @return 用户列表
     */
    @GetMapping
    @NameFill
    public PageResp<UnitUserDTO> listGrantUsers(@Valid PermUnitUserQueryVO vo) {
        return permUnitUserService.listGrantUsers(vo);
    }
}
