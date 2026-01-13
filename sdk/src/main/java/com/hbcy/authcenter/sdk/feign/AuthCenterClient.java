package com.hbcy.authcenter.sdk.feign;

import com.hbcy.authcenter.sdk.annotation.EnableHeaderPassthrough;
import com.hbcy.common.base.pojo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}
