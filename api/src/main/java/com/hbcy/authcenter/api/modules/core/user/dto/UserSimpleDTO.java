package com.hbcy.authcenter.api.modules.core.user.dto;

import lombok.Data;

/**
 * 考虑隐私设置，大部分情况下只显示这些信息
 *
 * @author 姚泰然
 * @date 2025-12-26 13:45
 */
@Data
public class UserSimpleDTO {
    /**
     * 用户id
     */
    private String id;
    /**
     * 姓名
     */
    private String realName;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 主职组织id
     */
    private String defaultOrg;
}
