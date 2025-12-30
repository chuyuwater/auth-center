package com.hbcy.authcenter.api.modules.core.perm.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PermUnitUpdateVO {
    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空")
    @Length(max = 50, message = "名称长度不能超过50")
    private String nameCn;
    /**
     * 说明
     */
    @Length(max = 200, message = "说明长度不能超过200")
    private String memo;
    /**
     * 分组id
     */
    @NotBlank(message = "归属分组不能为空")
    private String belongTo;
}
