package com.hbcy.authcenter.api.modules.core.perm.controller;

import com.hbcy.authcenter.api.modules.core.perm.dto.UserUnitDTO;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitUserService;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserUpdateVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUserUnitQueryVO;
import com.hbcy.common.base.pojo.BatchDeleteVO;
import com.hbcy.common.web.bean.NameFill;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户关联角色（租户侧）
 *
 * @author 姚泰然
 * @module perm
 * @date 2025-12-28
 */
@RestController
@RequestMapping("api/portal/v1/perm/user/units")
@Validated
public class PermUserUnitController {
    @Resource
    private PermUnitUserService permUnitUserService;

    /**
     * 用户授权清单
     * @param vo 查询条件
     * @return 用户关联的授权信息
     */
    @GetMapping
    @NameFill
    public List<UserUnitDTO> listUserUnits(@Valid PermUserUnitQueryVO vo) {
        return permUnitUserService.listUserUnits(vo);
    }

    /**
     * 批量关联用户到角色
     * 仅授予管理员
     *
     * @param vo 请求参数
     */
    @PostMapping("")
    public void addUsersToUnit(@Valid @RequestBody PermUnitUserUpdateVO vo) {
        permUnitUserService.addUsersToUnits(vo);
    }

    /**
     * 按授权id移除授权
     *
     * @param id 授权ID
     */
    @PostMapping("/delete")
    public void removeUsersFromUnit(@NotBlank(message = "id不能为空") String id) {
        permUnitUserService.deleteGrant(List.of(id));
    }

    /**
     * 按授权id批量移除授权
     *
     * @param vo 批量删除请求参数
     */
    @PostMapping("/delete-batch")
    public void batchDeleteUsers(@Valid @RequestBody BatchDeleteVO vo) {
        permUnitUserService.deleteGrant(vo.getIds());
    }
}
