package com.hbcy.authcenter.sdk.feign;

import com.hbcy.authcenter.sdk.annotation.EnableHeaderPassthrough;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author 姚泰然
 * @date 2026-01-13 16:32
 */
@FeignClient
@EnableHeaderPassthrough
public interface AuthCenterClient {
    
}
