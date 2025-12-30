package com.hbcy.authcenter.api.modules.core.app.controller;

import com.hbcy.authcenter.api.common.bean.NodeMoveVO;
import com.hbcy.authcenter.api.modules.core.app.model.App;
import com.hbcy.authcenter.api.modules.core.app.service.AppService;
import com.hbcy.authcenter.api.modules.core.app.vo.AppCreateVO;
import com.hbcy.authcenter.api.modules.core.app.vo.AppForbiddenVO;
import com.hbcy.authcenter.api.modules.core.app.vo.AppQueryVO;
import com.hbcy.authcenter.api.modules.core.app.vo.AppUpdateVO;
import com.hbcy.common.web.bean.NameFill;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用管理（门户侧）
 *
 * @author 姚泰然
 * @date 2025-12-23
 */
@RestController
@RequestMapping("api/portal/v1/app")
@Validated
public class AppController {

    @Resource
    private AppService appService;

    /**
     * 应用列表
     *
     * @param vo 查询条件
     * @return 全量列表
     */
    @GetMapping
    @NameFill
    public List<App> list(@RequestBody @Valid AppQueryVO vo) {
        return appService.list(vo);
    }

    /**
     * 根据ID获取应用信息
     *
     * @param id 应用ID
     * @return 应用信息
     */
    @GetMapping("/{id}")
    @NameFill
    public App getById(@PathVariable String id) {
        return appService.getById(id);
    }

    /**
     * 创建应用
     *
     * @param vo 应用信息
     * @return 创建后的应用信息
     */
    @PostMapping
    public App create(@RequestBody @Valid AppCreateVO vo) {
        return appService.create(vo);
    }

    /**
     * 拖动节点排序
     **/
    @PostMapping("/move")
    public void move(@RequestBody @Valid NodeMoveVO vo) {
        appService.move(vo);
    }

    /**
     * 更新应用
     *
     * @param id 应用ID
     * @param vo 应用信息
     * @return 更新后的应用信息
     */
    @PutMapping("/{id}")
    public App update(@PathVariable String id, @RequestBody @Valid AppUpdateVO vo) {
        return appService.update(vo, id);
    }

    /**
     * 切换应用状态（禁用/启用）
     *
     * @param vo 状态切换信息
     */
    @PostMapping("/status")
    public void switchStatus(@RequestBody @Valid AppForbiddenVO vo) {
        appService.switchStatus(vo);
    }

    /**
     * 删除应用
     *
     * @param id 应用ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        appService.delete(id);
    }
}
