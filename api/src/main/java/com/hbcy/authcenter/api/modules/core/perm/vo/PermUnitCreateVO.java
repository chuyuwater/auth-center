package com.hbcy.authcenter.api.modules.core.perm.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    /**
     * 内部使用
     */
    @JsonIgnore
    private Integer sysProtect = 0;
}
