package com.hbcy.authcenter.api.modules.core.auth.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2026-01-26 17:04
 */
@Data
public class UserAccessUpsertVO {
    @NotBlank(message = "keyName不能为空")
    private String keyName;
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    /**
     * 是否禁用
     */
    private Integer forbidden;
}
