package com.hbcy.authcenter.api.modules.core.app.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * @author 姚泰然
 * @date 2025-12-24
 */
@Data
public class ResourceTreeUpdateVO {
    @Length(max = 50, message = "中文名长度不能超过50")
    @NotBlank(message = "中文名不能为空")
    private String nameCn;

    @NotBlank(message = "自定义菜单ID不能为空")
    @Length(max = 50, message = "自定义菜单ID长度不能超过50")
    private String customId;

    @Range(min = 0, max = 2, message = "客户端类型只能为0-2")
    private Integer clientType = 0;

    @Length(max = 200, message = "图标地址长度不能超过200")
    private String icon;

    @Length(max = 255, message = "路由地址长度不能超过255")
    private String routeLink;

    @Range(min = 0, max = 1, message = "是否隐藏只能为0或1")
    private Integer hidden = 0;

    @Range(min = 0, max = 2, message = "显示级别只能为0-2")
    private Integer showLevel = 0;

    @Range(min = 0, max = 1, message = "是否禁用只能为0或1")
    private Integer forbidden = 0;

    @Range(min = 0, max = 2, message = "资源主体域只能为0-2")
    private Integer subDom = 1;

    @Length(max = 1000, message = "资源客体域长度不能超过1000")
    private String objDom = "";
}
