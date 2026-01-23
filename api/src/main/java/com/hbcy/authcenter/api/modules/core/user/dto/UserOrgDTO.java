package com.hbcy.authcenter.api.modules.core.user.dto;

import com.hbcy.common.db.dictvalue.DictInject;
import lombok.Data;

/**
 * 用户任职概况
 *
 * @author 姚泰然
 * @date 2025-12-26 14:08
 */
@Data
public class UserOrgDTO {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 组织id
     */
    private String orgId;
    /**
     * 组织名称
     */
    private String orgName;
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
    @DictInject(value = "ORG_CATEGORY")
    private Integer nodeCategory;
    /**
     * 是否主职组织
     */
    private Integer mainJob;
}
