package com.hbcy.authcenter.sdk.feign.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * 通过方案编码发送消息请求体
 */
@Data
public class SchemeSendVO {
    @NotBlank
    private String schemeNo;
    /** 动态传参时的接收人列表 */
    private Set<String> targetUsers;
    /** 变量值映射 */
    private Map<String, String> variables;
    /** 跳转地址 */
    private String jumpUrl;
    /** 原始报文 */
    private String originJson;
}
