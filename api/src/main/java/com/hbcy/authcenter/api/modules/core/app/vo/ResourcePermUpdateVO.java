package com.hbcy.authcenter.api.modules.core.app.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * 资源权限更新VO
 *
 * @author 姚泰然
 * @date 2025-12-24
 */
@Data
public class ResourcePermUpdateVO {
    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Length(max = 100, message = "权限名称长度不能超过100")
    private String permName;

    /**
     * 权限码
     */
    @Length(max = 100, message = "权限码长度不能超过100")
    private String permCode = "";

    /**
     * API请求方法
     * 0-GET, 1-POST, 2-PUT, 3-DELETE
     */
    @Range(min = 0, max = 3, message = "请求方法只能为0-3")
    private Integer apiMethod;

    /**
     * API路径
     */
    @Length(max = 255, message = "API路径长度不能超过255")
    @Pattern(regexp = "^/[\\w/\\-\\*\\?]*$", message = "API路径格式错误")
    private String apiPath;
}
