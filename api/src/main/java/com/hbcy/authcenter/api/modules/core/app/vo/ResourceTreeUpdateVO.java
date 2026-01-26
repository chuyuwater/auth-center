package com.hbcy.authcenter.api.modules.core.app.vo;

import com.hbcy.common.db.dictvalue.DictValid;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.util.List;

/**
 * 资源树更新VO
 *
 * @author 姚泰然
 * @date 2025-12-24
 */
@Data
public class ResourceTreeUpdateVO {
    /**
     * 父节点，创建时或更新后，节点固定在同级末尾。
     */
    private String parentId = "";
    /**
     * 中文名称
     */
    @Length(max = 50, message = "中文名长度不能超过50")
    @NotBlank(message = "中文名不能为空")
    private String nameCn;
    /**
     * 自定义菜单ID
     */
    @NotBlank(message = "自定义菜单ID不能为空")
    @Length(max = 50, message = "自定义菜单ID长度不能超过50")
    private String customId;
    /**
     * 客户端类型。字典项，key:CLIENT_TYPE
     */
    @DictValid(dictKey = "CLIENT_TYPE")
    private Integer clientType = 0;
    /**
     * 图标地址
     */
    @Length(max = 200, message = "图标地址长度不能超过200")
    private String icon;
    /**
     * 路由地址
     */
    @Length(max = 255, message = "路由地址长度不能超过255")
    private String routeLink;
    /**
     * 是否隐藏，0-否，1-是
     */
    @Range(min = 0, max = 1, message = "是否隐藏只能为0或1")
    private Integer hidden = 0;
    /**
     * 显示级别，字典项MENU_LEVEL
     */
    @DictValid(dictKey = "MENU_LEVEL")
    private Integer showLevel = 0;
    /**
     * 是否禁用，0-否，1-是
     */
    @Range(min = 0, max = 1, message = "是否禁用只能为0或1")
    private Integer forbidden = 0;
    /**
     * 子权限点，无id标识新建，有id标识更新
     */
    @Valid
    private List<ResourcePermCreateVO> subPerms;
}
