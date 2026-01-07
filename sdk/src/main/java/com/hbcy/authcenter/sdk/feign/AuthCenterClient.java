package com.hbcy.authcenter.sdk.feign;

import com.hbcy.authcenter.sdk.annotation.EnableHeaderPassthrough;
import com.hbcy.authcenter.sdk.feign.dto.ApiPermDTO;
import com.hbcy.common.web.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-07 15:00
 */
@FeignClient(name = "${service.url.portal-auth-center:portal-auth-center}")
@EnableHeaderPassthrough
public interface AuthCenterClient {
    /**
     * 获取应用所有权限id和对应的api列表
     */
    @GetMapping("inner/portal/v1/app/perm")
    ApiResponse<List<ApiPermDTO>> listAppPerms();

    /**
     * 获取所有租户的默认管理员
     */
    @GetMapping("inner/portal/v1/tenant/admin")
    ApiResponse<List<String>> listTenantAdmins();

    /**
     * 刷新用户权限缓存，供网关层调用
     */
    @PostMapping("inner/portal/v1/user/perm/refresh")
    ApiResponse<Object> refreshUserPerms();


}
