package com.hbcy.authcenter.api.modules.core.perm.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hbcy.common.db.dictvalue.DictField;
import lombok.Data;

/**
 * 客户端资源查询VO
 *
 * @author 姚泰然
 * @date 2025-12-30 10:09
 */
@Data
public class ClientResQueryVO {
    /**
     * 是否返回菜单关联的权限点
     */
    private boolean withPerm;
    /**
     * 1-pc，2-移动端
     */
    @DictField(dictKey = "CLIENT_TYPE")
    private Integer clientType;
    /**
     * 应用id，后端填充
     */
    @JsonIgnore
    private String appId;
    /**
     * 组织id，后端填充
     */
    @JsonIgnore
    private String orgId;
}
