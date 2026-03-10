package com.hbcy.authcenter.api.modules.core.app.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

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
    @NotBlank(message = "权限码不能为空")
    private String permCode;

    /**
     * 关联的API列表，可以为空（纯权限点不绑定API）。
     * 每次提交覆盖之前的配置。
     */
    @Valid
    private List<ResourcePermApiVO> apis;
}
