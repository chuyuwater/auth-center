package com.hbcy.authcenter.api.modules.sys.dict.controller;

import com.hbcy.authcenter.api.modules.sys.dict.model.SysDict;
import com.hbcy.authcenter.api.modules.sys.dict.service.SysDictService;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictCreateVO;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictQueryVO;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.tree.TreeNode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统字典
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
     * 创建系统字典
     *
     * @param vo 字典信息
     * @return 创建后的字典
     */
    @PostMapping
    public SysDict createSysDict(@Valid @RequestBody DictCreateVO vo) {
        return sysDictService.createSysDict(vo);
    }

    /**
     * 更新系统字典
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
     * 删除系统字典
     *
     * @param id 字典ID
     */
    @DeleteMapping("/{id}")
    public void deleteSysDict(@NotBlank(message = "ID不能为空") @PathVariable String id) {
        SysDict dict = sysDictService.getById(id);
        if (dict == null) {
            return;
        }
        sysDictService.deleteSysDict(dict.getFeatCode(), id);
    }

    /**
     * 字典详情
     *
     * @param id 字典ID
     * @return 字典信息
     */
    @GetMapping("/{id}")
    public SysDict getSysDictById(@NotBlank(message = "ID不能为空") @PathVariable String id) {
        return sysDictService.getById(id);
    }

    /**
     * 字典项列表
     *
     * @param featCode 业务编码
     * @return 字典列表
     */
    @GetMapping("/list")
    public List<SysDict> getSysDictsByFeatCode(@NotBlank(message = "featCode不能为空") String featCode) {
        return sysDictService.getSysDictsByFeatCode(featCode);
    }

    /**
     * 字典值映射
     *
     * @param featCode 业务编码
     * @return 字典值映射 map
     */
    @GetMapping("/map")
    public Map<String, String> getDictValueMapByFeatCode(@NotBlank(message = "featCode不能为空") String featCode) {
        return sysDictService.getDictValueMapByFeatCode(featCode);
    }

    /**
     * 搜索子节点（列表）
     *
     * @param vo 查询条件
     * @return 子节点列表
     */
    @GetMapping("/children")
    public List<SysDict> getChildrenRecursively(DictQueryVO vo) {
        if (StringUtils.isNotBlank(vo.getParentId())) {
            return sysDictService.getChildrenRecursively(vo.getParentId());
        } else if (StringUtils.isNotBlank(vo.getFeatCode()) && StringUtils.isNotBlank(vo.getValueStr())) {
            return sysDictService.getChildrenRecursively(vo.getFeatCode(), vo.getValueStr());
        }
        throw new ParamError("请传入parentId或featCode+valueStr");
    }

    /**
     * 搜索子节点（树状）
     *
     * @param vo 查询条件
     * @return 树结构
     */
    @GetMapping("/children/tree")
    public List<TreeNode<SysDict>> getChildrenAsTree(DictQueryVO vo) {
        TreeNode<SysDict> root = sysDictService.getChildrenAsTree(vo);
        if (root == null) return null;
        return root.getChildren();
    }
}
