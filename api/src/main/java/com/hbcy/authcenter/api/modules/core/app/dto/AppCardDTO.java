package com.hbcy.authcenter.api.modules.core.app.dto;

import lombok.Data;

/**
 * app卡片
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
     * logo图片的URL地址
     */
    private String logo;
    /**
     * 应用描述
     */
    private String memo;
    /**
     * 是否禁用, 0-启用，1-禁用
     */
    private Integer forbidden;
}
