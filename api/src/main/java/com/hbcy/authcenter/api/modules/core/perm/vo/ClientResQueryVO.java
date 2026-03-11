package com.hbcy.authcenter.api.modules.core.perm.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hbcy.authcenter.api.common.enums.TreeQueryLevelEnum;
import com.hbcy.common.db.dictvalue.DictValid;
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
    private Boolean withPerm = false;
    /**
     * 1-pc，2-移动端
     */
    @DictValid(dictKey = "CLIENT_TYPE")
    private Integer clientType;
    /**
     * 应用id，不传则搜索当前应用
     */
    private String appId;
    /**
     * 父级菜单自定义id
     */
    private String parentCustomId;
    /**
     * 父级菜单查询方式
     * 1-下级，2-本下，默认1
     */
    private Integer parentLevel = TreeQueryLevelEnum.CHILD.getCode();
    /**
     * 资源类型，0-页面，1-按钮
     */
    private Integer resType;
    /**
     * 组织id，后端填充
     */
    @JsonIgnore
    private String orgId;
}
