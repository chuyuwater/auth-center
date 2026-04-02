package com.hbcy.authcenter.api.modules.core.inner.controller;

import com.hbcy.authcenter.api.modules.core.inner.service.SDKService;
import com.hbcy.authcenter.api.modules.core.perm.service.PermUnitUserService;
import com.hbcy.authcenter.api.modules.minor.msg.service.UserMsgService;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgCreateVO;
import com.hbcy.authcenter.api.modules.minor.todo.service.UserTodoService;
import com.hbcy.authcenter.sdk.feign.vo.TodoCreateVO;
import com.hbcy.authcenter.sdk.feign.vo.TodoUpdateVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SDK专用接口
 * 这些接口仅供内网或租户管理员通过ak调用
 * @author 姚泰然
 * @module sdk
 * @date 2026-03-05 09:08
 */
@Validated
@RestController
@RequestMapping("/api/portal/v1/sdk")
public class SDKController {

    @Resource
    private SDKService sdkService;

    @Resource
    private PermUnitUserService permUnitUserService;

    @Resource
    private UserMsgService userMsgService;

    @Resource
    private UserTodoService userTodoService;

    /**
     * 获取组织id对应的组织名称
     * @param orgIds 组织id
     * @param fullName 是否返回全称
     * @return 字典
     */
    @GetMapping("/org/names")
    public Map<String, String> getOrgNames(@RequestParam List<String> orgIds, boolean fullName) {
        return sdkService.getOrgNames(orgIds, fullName);
    }

    /**
     * 获取用户id对应的名称
     * @param userIds 组织id
     * @return 字典
     */
    @GetMapping("/user/names")
    public Map<String, String> getOrgNames(@RequestParam Set<String> userIds) {
        return sdkService.getUserNames(userIds);
    }

    /**
     * 获取用户在指定组织、指定应用下是否有某个权限码
     * @param userId 用户id
     * @param orgId 组织id
     *
     * @return 是否拥有权限
     */
    @GetMapping("/perm/check")
    public boolean hasAnyPerm(@NotBlank(message = "用户id不能为空") String userId,
                              @NotBlank(message = "组织id不能为空") String orgId,
                              @NotBlank(message = "应用id不能为空") String appId,
                              @NotBlank(message = "权限码不能为空") String permCode) {
        return permUnitUserService.checkPerm(userId, orgId, appId, List.of(permCode)).getOrDefault(
                permCode, false);
    }

    /**
     * 批量检查用户在指定组织、指定应用下是否有某个权限码
     * @param userId 用户id
     * @param orgId 组织id
     * @param permCodes 权限码列表
     * @return 权限码和是否有权限的映射
     */
    @GetMapping("/perm/check-batch")
    public Map<String, Boolean> checkBatchPerm(@NotBlank(message = "用户id不能为空") String userId,
                                               @NotBlank(message = "组织id不能为空") String orgId,
                                               @NotBlank(message = "应用id不能为空") String appId,
                                               @NotEmpty(message = "权限码列表不能为空") List<String> permCodes) {
        return permUnitUserService.checkPerm(userId, orgId, appId, permCodes);
    }

    /**
     * 获取用户有指定权限码的组织
     * @param userId 用户id
     * @param appId 应用id
     * @param permCode 权限码
     * @param parentOrgId 父级组织id，查询本下，不传则查询租户所有满足条件的组织
     * @return 组织id列表
     */
    @GetMapping("/perm/grant-orgs")
    public List<String> listGrantOrgs(@NotBlank(message = "用户id不能为空") String userId,
                                      @NotBlank(message = "应用id不能为空") String appId,
                                      @NotBlank(message = "权限码不能为空") String permCode,
                                      String parentOrgId) {
        return sdkService.listGrantOrgs(userId, appId, permCode, parentOrgId);
    }

    /**
     * 创建消息
     */
    @PostMapping("/msg")
    public void batchCreateMsg(@Valid @RequestBody UserMsgCreateVO vo) {
        userMsgService.batchCreateMsg(vo);
    }

    /**
     * 创建待办，一般是流程引擎调用
     */
    @PostMapping("/todo")
    public void batchCreateTodo(@Valid @RequestBody TodoCreateVO vo) {
        userTodoService.batchCreateTodo(vo);
    }

    /**
     * 更新待办状态，一般是流程引擎调用
     */
    @PutMapping("/todo/update-state")
    public void updateState(@Valid @RequestBody TodoUpdateVO vo) {
        userTodoService.updateState(vo);
    }
}
