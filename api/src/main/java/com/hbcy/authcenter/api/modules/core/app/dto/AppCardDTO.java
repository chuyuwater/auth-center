package com.hbcy.authcenter.api.modules.core.app.dto;

import lombok.Data;

/**
 * app基本信息
 *
 * @author 姚泰然
 * @date 2025-12-24 08:53
 */
@Data
public class AppCardDTO {

    /**
     * 应用id
     */
    private String appId;
    /**
     * 应用名称
     */
    private String nameCn;

    /**
     * 图标
     */
    private String icon;
    /**
     * 应用描述
     */
    private String memo;
    /**
     * 应用类型：1-平台应用，2-外部应用
     */
    private Integer appType;
    /**
     * 外部应用时的应用URL，用于三方认证等
     */
    private String appUrl;
}
