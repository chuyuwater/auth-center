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
}
