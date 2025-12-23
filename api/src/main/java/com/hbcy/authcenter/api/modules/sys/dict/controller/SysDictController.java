package com.hbcy.authcenter.api.modules.sys.dict.controller;

import com.hbcy.authcenter.api.modules.sys.dict.model.SysDict;
import com.hbcy.authcenter.api.modules.sys.dict.service.SysDictService;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictQueryVO;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictUpsertVO;
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
 * @author 姚泰然
 * @date 2025-12-22 13:45
 */
@RestController
@RequestMapping("api/portal/v1/sys/dict")
@Validated
public class SysDictController {

    @Resource
    private SysDictService sysDictService;

    @PostMapping
    public SysDict createSysDict(@Valid @RequestBody DictUpsertVO vo) {
        return sysDictService.createSysDict(vo);
    }

    @PutMapping("/{id}")
    public SysDict updateSysDict(@NotBlank(message = "ID不能为空") @PathVariable String id,
                                 @Valid @RequestBody DictUpsertVO vo) {
        return sysDictService.updateSysDict(id, vo);
    }

    @DeleteMapping("/{id}")
    public void deleteSysDict(@NotBlank(message = "ID不能为空") @PathVariable String id) {
        SysDict dict = sysDictService.getById(id);
        if (dict == null) {
            return;
        }
        sysDictService.deleteSysDict(dict.getFeatCode(), id);
    }

    @GetMapping("/{id}")
    public SysDict getSysDictById(@NotBlank(message = "ID不能为空") @PathVariable String id) {
        return sysDictService.getById(id);
    }

    @GetMapping("/list")
    public List<SysDict> getSysDictsByFeatCode(@NotBlank(message = "featCode不能为空") String featCode) {
        return sysDictService.getSysDictsByFeatCode(featCode);
    }

    @GetMapping("/map")
    public Map<String, String> getDictValueMapByFeatCode(@NotBlank(message = "featCode不能为空") String featCode) {
        return sysDictService.getDictValueMapByFeatCode(featCode);
    }

    @GetMapping("/children")
    public List<SysDict> getChildrenRecursively(DictQueryVO vo) {
        if (StringUtils.isNotBlank(vo.getParentId())) {
            return sysDictService.getChildrenRecursively(vo.getParentId());
        } else if (StringUtils.isNotBlank(vo.getFeatCode()) && StringUtils.isNotBlank(vo.getValueStr())) {
            return sysDictService.getChildrenRecursively(vo.getFeatCode(), vo.getValueStr());
        }
        throw new ParamError("请传入parentId或featCode+valueStr");
    }

    @GetMapping("/children/tree")
    public TreeNode<SysDict> getChildrenAsTree(DictQueryVO vo) {
        return sysDictService.getChildrenAsTree(vo);
    }
}
