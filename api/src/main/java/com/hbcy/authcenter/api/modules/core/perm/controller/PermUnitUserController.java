package com.hbcy.authcenter.api.modules.core.perm.controller;

import com.hbcy.authcenter.api.modules.core.perm.dto.UnitUserDTO;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitUserService;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserUpdateVO;
import com.hbcy.common.base.pojo.BatchDeleteVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/portal/v1/perm/unit/users")
@Validated
public class PermUnitUserController {

    @Resource
    private PermUnitUserService permUnitUserService;

    @PostMapping
    public void addUsersToUnit(@Valid @RequestBody PermUnitUserUpdateVO vo) {
        permUnitUserService.addUsersToUnit(vo);
    }

    @DeleteMapping
    public void removeUsersFromUnit(@RequestParam String grantId) {
        permUnitUserService.deleteGrant(List.of(grantId));
    }

    @PostMapping("/batch-delete")
    public void batchDeleteUsers(@Valid @RequestBody BatchDeleteVO vo) {
        permUnitUserService.deleteGrant(vo.getIds());
    }

    @GetMapping
    public List<UnitUserDTO> listGrantUsers(@Valid PermUnitUserQueryVO vo) {
        return permUnitUserService.listGrantUsers(vo);
    }
}
