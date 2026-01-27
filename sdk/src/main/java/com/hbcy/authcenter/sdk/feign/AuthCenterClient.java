package com.hbcy.authcenter.sdk.feign;

import com.hbcy.authcenter.sdk.config.PortalFeignConfig;
import com.hbcy.authcenter.sdk.feign.dto.OrgNodeDTO;
import com.hbcy.authcenter.sdk.feign.dto.SysDictDTO;
import com.hbcy.authcenter.sdk.feign.vo.MsgCreateVO;
import com.hbcy.authcenter.sdk.feign.vo.TodoCreateVO;
import com.hbcy.authcenter.sdk.feign.vo.TodoUpdateVO;
import com.hbcy.common.base.pojo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author 姚泰然
 * @date 2026-01-13 16:32
 */
@FeignClient(name = "portal-auth-center", configuration = PortalFeignConfig.class)
public interface AuthCenterClient {
    /**
     * 获取当前用户、当前组织下、当前应用（或指定菜单下）的权限码
     * 需要将用户数据通过header透传过来
     * 建议调用方缓存这个结果一段时间
     *
     * @param resId 资源id，传入null则返回整个app的所有权限码
     * @return 权限码集合
     */
    @GetMapping("/api/portal/v1/client/permCode")
    ApiResponse<Set<String>> listPermCode(@RequestParam String resId);

    /**
     * 检查当前用户在当前组织、当前app下是否有指定权限码
     * 该结果会使用与网关侧一致的缓存，因此判断结果与网关放行一致
     *
     * @param permCode 权限码，如sys:user:create
     * @return true/false
     */
    @GetMapping("/api/portal/v1/client/permCheck")
    ApiResponse<Boolean> checkPerm(@RequestParam String permCode);

    /**
     * 获取组织节点详情
     * @param id 节点ID
     * @return 节点信息
     */
    @GetMapping("/api/portal/v1/org/node/{id}")
    ApiResponse<OrgNodeDTO> getOrgNodeInfo(@PathVariable String id);

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
    @PostMapping("/api/portal/v1/user/inbox")
    ApiResponse<Object> createMsg(@RequestBody MsgCreateVO vo);

    /**
     * 创建待办
     */
    @PostMapping("/api/portal/v1/user/todo")
    ApiResponse<Object> createTodo(@RequestBody TodoCreateVO vo);

    /**
     * 更新待办状态
     */
    @PostMapping("/api/portal/v1/user/todo/update-state")
    ApiResponse<Object> updateTodoState(@RequestBody TodoUpdateVO vo);
}
