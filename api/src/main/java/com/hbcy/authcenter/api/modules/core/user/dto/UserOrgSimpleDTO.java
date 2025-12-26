package com.hbcy.authcenter.api.modules.core.user.dto;

/**
 * 用户任职概况
 *
 * @author 姚泰然
 * @date 2025-12-26 14:08
 */
public class UserOrgSimpleDTO {
    /**
     * 组织id
     */
    private String id;
    /**
     * 全路径（id）
     */
    private String idPath;
    /**
     * 全路径（名字）
     */
    private String namePath;
    /**
     * 组织类别
     */
    private String category;
    /**
     * 是否主职
     */
    private boolean main;
}
