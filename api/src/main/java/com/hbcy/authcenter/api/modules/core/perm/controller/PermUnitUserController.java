package com.hbcy.authcenter.api.modules.core.perm.controller;

import com.hbcy.authcenter.api.modules.core.perm.dto.UnitUserDTO;
import com.hbcy.authcenter.api.modules.core.perm.dto.UserUnitDTO;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitUserService;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserUpdateVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUserUnitQueryVO;
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
@RequestMapping("api/portal/v1/perm")
@Validated
public class PermUnitUserController {

    @Resource
    private PermUnitUserService permUnitUserService;

    /**
     * 批量关联用户到角色
     * 仅授予管理员
     *
     * @param vo 请求参数
     */
    @PostMapping("/unit/users")
    public void addUsersToUnit(@Valid @RequestBody PermUnitUserUpdateVO vo) {
        permUnitUserService.addUsersToUnits(vo);
    }

    /**
     * 按授权id移除授权
     *
     * @param grantId 授权ID
     */
    @DeleteMapping("/unit/users")
    public void removeUsersFromUnit(@RequestParam String grantId) {
        permUnitUserService.deleteGrant(List.of(grantId));
    }

    /**
     * 按授权id批量移除授权
     *
     * @param vo 批量删除请求参数
     */
    @PostMapping("/unit/users/batch-delete")
    public void batchDeleteUsers(@Valid @RequestBody BatchDeleteVO vo) {
        permUnitUserService.deleteGrant(vo.getIds());
    }

    /**
     * 授权用户列表
     *
     * @param vo 查询条件
     * @return 用户列表
     */
    @GetMapping("/unit/users")
    @NameFill
    public PageResp<UnitUserDTO> listGrantUsers(@Valid PermUnitUserQueryVO vo) {
        return permUnitUserService.listGrantUsers(vo);
    }

    /**
     * 用户授权清单
     * @param vo 查询条件
     * @return 用户关联的授权信息
     */
    @GetMapping("user/units")
    @NameFill
    public List<UserUnitDTO> listUserUnits(@Valid PermUserUnitQueryVO vo) {
        return permUnitUserService.listUserUnits(vo);
    }
}
