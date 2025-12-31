package com.hbcy.authcenter.api.modules.core.perm.controller;

import com.hbcy.authcenter.api.modules.core.app.dto.ResTreeDTO;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitResourceService;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitAppDTO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitResourceQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitResourceSaveVO;
import com.hbcy.common.base.tree.TreeNode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限封装（租户侧）
 *
 * @author 姚泰然
 * @date 2025-12-28
 */
@RestController
@RequestMapping("api/portal/v1/perm/unit")
@Validated
public class PermUnitResourceController {

    @Resource
    private PermUnitResourceService permUnitResourceService;

    /**
     * 全量创建/更新权限单元关联的权限资源
     *
     * @param vo 详情
     */
    @PostMapping("/resources")
    public void addCodesToUnit(@Valid @RequestBody PermUnitResourceSaveVO vo) {
        permUnitResourceService.addCodesToUnit(vo);
    }

    /**
     * 获取权限单元关联的应用清单
     *
     * @param unitId 权限单元id（角色或策略）
     * @return 应用清单（含未封装）
     */
    @GetMapping("/app")
    public List<PermUnitAppDTO> listUnitApp(@RequestParam("unitId") String unitId) {
        return permUnitResourceService.listApps(unitId);
    }

    /**
     * 获取权限单元关联的权限树
     *
     * @param vo 查询条件
     * @return 权限树
     */
    @GetMapping("/resources")
    public List<TreeNode<ResTreeDTO>> listUnitResources(@Valid PermUnitResourceQueryVO vo) {
        return permUnitResourceService.listUnitResources(vo);
    }
}
