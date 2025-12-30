package com.hbcy.authcenter.api.modules.core.app.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 姚泰然
 * @date 2025-12-24
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ResourcePermCreateVO extends ResourcePermUpdateVO {
    /**
     * 关联的资源id
     */
    @NotBlank(message = "资源id不能为空")
    private String resId;
}
