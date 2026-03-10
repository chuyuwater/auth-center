package com.hbcy.authcenter.api.modules.core.app.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * 权限点关联的API配置VO
 *
 * @author 姚泰然
 * @date 2026-03-10
 */
@Data
public class ResourcePermApiVO {
    /**
     * API请求方法
     * 0-GET, 1-POST, 2-PUT, 3-DELETE
     */
    @NotNull(message = "请求方法不能为空")
    @Range(min = 0, max = 3, message = "请求方法只能为0-3")
    private Integer apiMethod;

    /**
     * API路径，支持ant通配符
     */
    @NotNull(message = "API路径不能为空")
    @Length(max = 255, message = "API路径长度不能超过255")
    @Pattern(regexp = "^$|^/api/[\\w/\\-*?]*$", message = "API必须以/api开头")
    private String apiPath;
}
