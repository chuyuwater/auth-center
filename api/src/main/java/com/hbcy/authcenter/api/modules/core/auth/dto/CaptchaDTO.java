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
    private String id;
    private String base64;
}
