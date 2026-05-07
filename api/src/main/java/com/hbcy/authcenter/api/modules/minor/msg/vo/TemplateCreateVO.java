package com.hbcy.authcenter.api.modules.minor.msg.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增模板请求体
 */
@Data
public class TemplateCreateVO {
    @NotBlank @Size(max = 20)
    private String templateName;
    @NotBlank
    private String msgType;
    @NotBlank
    private String bizEntity;
    @NotBlank
    private String groupId;
    @Size(max = 200)
    private String description;
    @NotEmpty
    private List<TemplateContentVO> contents;
}
