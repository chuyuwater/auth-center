package com.hbcy.authcenter.api.modules.minor.user.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户快捷入口
 *
 * @author 姚泰然
 * @date 2026-03-26 11:20
 */
@Data
@Accessors(chain = true)
public class UserQuickLinkDTO {
    /**
     * 快捷入口记录id
     */
    private String id;
    /**
     * 菜单资源id
     */
    private String resId;
    /**
     * 应用id
     */
    private String appId;
    /**
     * 菜单名称
     */
    private String nameCn;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 菜单自定义路由标识
     */
    private String customId;
    /**
     * 菜单路由
     */
    private String routeLink;
    /**
     * 显示顺序
     */
    private Integer showOrder;
}
