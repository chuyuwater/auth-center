package com.hbcy.authcenter.api.modules.minor.msg.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改模板基本信息请求体
 */
@Data
public class TemplateUpdateVO {
    @Size(max = 20)
    private String templateName;
    private String msgType;
    private String bizEntity;
    private String groupId;
    @Size(max = 200)
    private String description;
}
