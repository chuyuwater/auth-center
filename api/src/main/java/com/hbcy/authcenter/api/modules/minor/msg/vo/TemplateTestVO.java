package com.hbcy.authcenter.api.modules.minor.msg.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 测试发送模板消息
 */
@Data
public class TemplateTestVO {
    @NotBlank
    private String receiver;
    private Map<String, String> variables;
}
