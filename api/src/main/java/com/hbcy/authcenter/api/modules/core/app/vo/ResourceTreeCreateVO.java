package com.hbcy.authcenter.api.modules.core.app.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * 资源树创建VO
 *
 * @author 姚泰然
 * @date 2025-12-24
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ResourceTreeCreateVO extends ResourceTreeUpdateVO {
    /**
     * 应用ID
     */
    @NotBlank(message = "应用ID不能为空")
    @Length(max = 50, message = "应用ID长度不能超过50")
    private String appId;
}
