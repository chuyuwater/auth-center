package com.hbcy.authcenter.sdk.feign;

import com.hbcy.authcenter.sdk.config.PortalFeignConfig;
import com.hbcy.authcenter.sdk.feign.dto.OrgNodeDTO;
import com.hbcy.authcenter.sdk.feign.dto.SysDictDTO;
import com.hbcy.authcenter.sdk.feign.vo.MsgCreateVO;
import com.hbcy.authcenter.sdk.feign.vo.OrgNodeQueryVO;
import com.hbcy.authcenter.sdk.feign.vo.TodoCreateVO;
import com.hbcy.authcenter.sdk.feign.vo.TodoUpdateVO;
import com.hbcy.common.base.pojo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * sdk，支持外部调用
 *
 * @author 姚泰然
 * @date 2026-01-13 16:32
 */
@FeignClient(name = "portal-auth-center", url = "${app.portal.url:}", configuration = PortalFeignConfig.class)
public interface AuthCenterClient {
    /**
     * 获取当前用户、当前组织下、当前应用（或指定菜单下）的权限码
     * 需要将用户数据通过header透传过来
     * 建议调用方缓存这个结果一段时间
     *
     * @param customId 前端自定义的菜单id
     * @return 权限码集合
     */
    @GetMapping("/api/portal/v1/client/perm-code")
    ApiResponse<Set<String>> listPermCode(@RequestParam String customId);

    /**
     * 检查当前用户在当前组织、当前app下是否有指定权限码
     * 该结果会使用与网关侧一致的缓存，因此判断结果与网关放行一致
     *
     * @param permCode 权限码，如sys:user:create
     * @return true/false
     */
    @GetMapping("/api/portal/v1/client/perm-check")
    ApiResponse<Boolean> checkPerm(@RequestParam String permCode);

    /**
     * 获取组织节点详情
     * 建议缓存
     * @param id 节点ID
     * @return 节点信息
     */
    @GetMapping("/api/portal/v1/org/node/{id}")
    ApiResponse<OrgNodeDTO> getOrgNodeInfo(@PathVariable String id);

    /**
     * 获取组织（部门）名称
     * @param orgIds 组织ID列表
     * @param fullName 是否返回全称
     * @return 组织id与名称的映射
     */
    @GetMapping("/api/portal/v1/sdk/org/names")
    ApiResponse<Map<String, String>> getOrgNames(@RequestParam Collection<String> orgIds, @RequestParam boolean fullName);

    /**
     * 查询组织节点
     * 租户id通过header传入
     * @param vo 查询条件
     * @return 组织节点列表
     */
    @GetMapping("api/portal/v1/org/list")
    ApiResponse<List<OrgNodeDTO>> listOrgNodes(@RequestParam OrgNodeQueryVO vo);

    /**
     * 获取用户名称
     * @param userIds 用户id
     * @return 用户id与名称的映射
     */
    @GetMapping("/api/portal/v1/sdk/user/names")
    ApiResponse<Map<String, String>> getUserNames(@RequestParam Collection<String> userIds);

    /**
     * 检查指定用户在指定组织、指定app下是否有指定权限码
     * 该结果会使用与网关侧一致的缓存，因此判断结果与网关放行一致
     *
     * @param userId 用户id
     * @param orgId 组织id
     * @param appId 应用编码
     * @param permCode 权限码，如sys:user:create
     * @return true/false
     */
    @GetMapping("/api/portal/v1/sdk/perm/check")
    ApiResponse<Boolean> checkAnyPerm(
            @RequestParam String userId,
            @RequestParam String orgId,
            @RequestParam String appId,
            @RequestParam String permCode);

    /**
     * 获取用户被赋予了某个权限的所有组织id
     * @param userId 用户id
     * @param appId 应用编码
     * @param permCode 权限码，如sys:user:create
     * @param parentOrgId 父级组织id，为空则查询用户在租户里的所有有权组织
     * @return 授权组织id列表
     */
    @GetMapping("/api/portal/v1/sdk/perm/grant-orgs")
    ApiResponse<List<String>> listGrantOrgs(
            @RequestParam String userId,
            @RequestParam String appId,
            @RequestParam String permCode,
            @RequestParam String parentOrgId
    );

    /**
     * 分组下的字典项列表
     * 用于列表状字典的全量查询，或树状字典的分级展开查询，有缓存
     * @param featCode 字典类型编码（非id）
     * @param parentId 父节点ID，为空则查询分组下的所有字典项
     * @return 字典项列表
     */
    @GetMapping("/api/portal/v1/sys/dict/list")
    ApiResponse<List<SysDictDTO>> listDictByFeatCode(@RequestParam String featCode, @RequestParam String parentId);

    /**
     * 创建消息
     */
    @PostMapping("/api/portal/v1/sdk/inbox")
    ApiResponse<Object> createMsg(@RequestBody MsgCreateVO vo);

    /**
     * 创建待办，一般是流程引擎调用
     */
    @PostMapping("/api/portal/v1/sdk/todo")
    ApiResponse<Object> createTodo(@RequestBody TodoCreateVO vo);

    /**
     * 更新待办状态，一般是流程引擎调用
     */
    @PutMapping("/api/portal/v1/sdk/todo/update-state")
    ApiResponse<Object> updateTodoState(@RequestBody TodoUpdateVO vo);
}
