package com.hbcy.authcenter.api.modules.sys.dict.controller;

import com.hbcy.authcenter.api.modules.sys.dict.model.SysDict;
import com.hbcy.authcenter.api.modules.sys.dict.service.SysDictGroupService;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictGroupCreateVO;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictGroupUpdateVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典类型（分组）管理
 *
 * @module sys
 * @author 姚泰然
 * @date 2026-01-15 09:04
 */
@RestController
@RequestMapping("api/portal/v1/sys/dict/group")
public class SysDictGroupController {
    @Resource
    private SysDictGroupService sysDictGroupService;

    /**
     * 创建字典类型
     * @param vo 参数
     * @return 创建结果
     */
    @PostMapping
    public SysDict createGroup(@Valid @RequestBody DictGroupCreateVO vo) {
        return sysDictGroupService.createGroup(vo);
    }

    /**
     * 更新字典类型
     * @param vo 参数
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public SysDict updateGroup(@PathVariable String id, @Valid @RequestBody DictGroupUpdateVO vo) {
        return sysDictGroupService.updateGroup(id, vo);
    }

    /**
     * 删除字典类型
     * @param id 分组id
     */
    @DeleteMapping("/{id}")
    public void deleteGroup(@PathVariable String id) {
        sysDictGroupService.deleteGroup(id);
    }

    /**
     * 字典类型列表
     * @param appId 应用id，不填则返回所有
     * @return 字典类型列表
     */
    @GetMapping
    public List<SysDict> queryGroups(String appId) {
        return sysDictGroupService.queryGroups(appId);
    }
}
