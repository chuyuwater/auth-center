package com.hbcy.authcenter.api.modules.core.app.controller;

import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.api.modules.core.app.service.ResourcePermService;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermCreateVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermQueryVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermUpdateVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源权限相关api
 *
 * @author 姚泰然
 * @date 2025-12-24
 */
@RestController
@RequestMapping("api/portal/v1/resource/perm")
@Validated
public class ResourcePermController {

    @Resource
    private ResourcePermService resourcePermService;

    @GetMapping
    public List<ResourcePerm> list(@RequestBody @Valid ResourcePermQueryVO vo) {
        return resourcePermService.list(vo);
    }

    @GetMapping("/{id}")
    public ResourcePerm getById(@PathVariable String id) {
        return resourcePermService.getById(id);
    }

    @PostMapping
    public ResourcePerm create(@RequestBody @Valid ResourcePermCreateVO vo) {
        return resourcePermService.create(vo);
    }

    @PutMapping("/{id}")
    public ResourcePerm update(@PathVariable String id, @RequestBody @Valid ResourcePermUpdateVO vo) {
        return resourcePermService.update(vo, id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        resourcePermService.delete(id);
    }
}
