package com.hbcy.authcenter.sdk.feign;

import com.hbcy.authcenter.sdk.annotation.EnableHeaderPassthrough;
import com.hbcy.authcenter.sdk.feign.dto.SysDictDTO;
import com.hbcy.common.base.pojo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

/**
 * @author 姚泰然
 * @date 2026-01-13 16:32
 */
@FeignClient(name = "portal-auth-center")
@EnableHeaderPassthrough
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
     * 分组下的字典项列表
     * 用于列表状字典的全量查询，或树状字典的分级展开查询，有缓存
     * @param featCode 字典类型编码（非id）
     * @param parentId 父节点ID，为空则查询分组下的所有字典项
     * @return 字典项列表
     */
    @GetMapping("/api/portal/v1/sys/dict/list")
    ApiResponse<List<SysDictDTO>> listDictByFeatCode(@RequestParam String featCode, @RequestParam String parentId);
}
