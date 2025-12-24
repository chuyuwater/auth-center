package com.hbcy.authcenter.api.modules.core.app.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * @author 姚泰然
 * @date 2025-12-24
 */
@Data
public class ResourcePermUpdateVO {

    @Range(min = 0, max = 1, message = "是否必选只能为0或1")
    private int required = 0;

    @NotBlank(message = "权限名称不能为空")
    @Length(max = 100, message = "权限名称长度不能超过100")
    private String permName;

    @NotBlank(message = "权限码不能为空")
    @Length(max = 100, message = "权限码长度不能超过100")
    private String permCode;

    @NotNull(message = "请求方法不能为空")
    @Range(min = 0, max = 3, message = "请求方法只能为0-3")
    private Integer apiMethod;

    @NotBlank(message = "API路径不能为空")
    @Length(max = 255, message = "API路径长度不能超过255")
    private String apiPath;
}
