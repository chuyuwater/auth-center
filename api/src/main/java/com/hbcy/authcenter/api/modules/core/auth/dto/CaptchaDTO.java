package com.hbcy.authcenter.api.modules.core.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 图形验证码信息
 *
 * @author 姚泰然
 * @date 2025-12-30 17:28
 */
@Data
@AllArgsConstructor
public class CaptchaDTO {
    /**
     * 验证码ID
     */
    private String id;
    /**
     * 验证码图片Base64
     */
    private String base64;
}
