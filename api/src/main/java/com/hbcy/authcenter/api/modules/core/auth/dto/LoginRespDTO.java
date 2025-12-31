package com.hbcy.authcenter.api.modules.core.auth.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 登录响应信息
 *
 * @author 姚泰然
 * @date 2025-12-30
 */
@Data
@Accessors(chain = true)
public class LoginRespDTO {
    /**
     * 登录令牌
     */
    private String token;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 头像地址
     */
    private String avatar;
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
}
