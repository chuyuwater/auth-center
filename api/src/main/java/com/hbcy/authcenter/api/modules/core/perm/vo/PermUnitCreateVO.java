package com.hbcy.authcenter.api.modules.core.perm.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PermUnitCreateVO extends PermUnitUpdateVO {
    /**
     * 分组id
     */
    @NotBlank(message = "归属分组不能为空")
    private String belongTo;
}
