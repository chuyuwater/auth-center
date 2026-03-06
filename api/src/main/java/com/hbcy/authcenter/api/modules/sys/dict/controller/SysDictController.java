package com.hbcy.authcenter.api.modules.sys.dict.controller;

import com.hbcy.authcenter.api.common.bean.NodeMoveVO;
import com.hbcy.authcenter.api.modules.sys.dict.model.SysDict;
import com.hbcy.authcenter.api.modules.sys.dict.service.SysDictService;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictCreateVO;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictQueryVO;
import com.hbcy.common.base.tree.TreeNode;
import com.hbcy.common.web.bean.NameFill;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典项管理
 *
 * @author 姚泰然
 * @module sys
 * @date 2025-12-22 13:45
 */
@RestController
@RequestMapping("api/portal/v1/sys/dict")
@Validated
public class SysDictController {

    @Resource
    private SysDictService sysDictService;

    /**
     * 创建字典项
     *
     * @param vo 字典信息
     * @return 创建后的字典
     */
    @PostMapping
    public SysDict createSysDict(@Valid @RequestBody DictCreateVO vo) {
        return sysDictService.createSysDict(vo);
    }

    /**
     * 更新字典项
     *
     * @param id 字典ID
     * @param vo 字典信息
     * @return 更新后的字典
     */
    @PutMapping("/{id}")
    public SysDict updateSysDict(@NotBlank(message = "ID不能为空") @PathVariable String id,
                                 @Valid @RequestBody DictCreateVO vo) {
        return sysDictService.updateSysDict(id, vo);
    }

    /**
     * 删除字典项
     *
     * @param id 字典ID
     */
    @DeleteMapping("/{id}")
    public void deleteSysDict(@NotBlank(message = "ID不能为空") @PathVariable String id) {
        sysDictService.deleteSysDict(id);
    }

    /**
     * 字典项详情
     *
     * @param id 字典ID
     * @return 字典信息
     */
    @GetMapping("/{id}")
    @NameFill
    public SysDict getSysDictById(@NotBlank(message = "ID不能为空") @PathVariable String id) {
        return sysDictService.getById(id);
    }

    /**
     * 分组下的字典项列表
     * 用于列表状字典的全量查询，或树状字典的分级展开查询，有缓存
     * @param featCode 字典类型编码（非id）
     * @param parentId 父节点ID，为空则查询分组下的所有字典项
     * @return 字典项列表
     */
    @GetMapping("/list")
    public List<SysDict> listDictByFeatCode(@NotBlank(message = "featCode不能为空") String featCode,
                                            String parentId) {
        return sysDictService.listDictByFeatCode(featCode, parentId);
    }

    /**
     * 分组下的字典树
     * 用于列表状或树状字典的全量查询，允许模糊搜索，无缓存
     *
     * @param vo 查询条件
     * @return 子节点列表
     */
    @GetMapping("/children")
    @NameFill
    public List<TreeNode<SysDict>> getChildrenRecursively(DictQueryVO vo) {
        return sysDictService.getChildrenAsTree(vo);
    }

    /**
     * 移动字典项
     * 只能在同级移动，后端忽略parentId参数
     * @param vo 移动详情
     */
    @PutMapping("/move")
    public void move(@Valid @RequestBody NodeMoveVO vo) {
        sysDictService.move(vo);
    }
}
