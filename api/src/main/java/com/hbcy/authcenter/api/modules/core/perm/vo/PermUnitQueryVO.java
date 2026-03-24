package com.hbcy.authcenter.api.modules.core.perm.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

/**
 * 权限单元查询VO
 *
 * @author 姚泰然
 * @date 2025-12-28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PermUnitQueryVO extends PageVO {
    /**
     * 名称模糊查询
     */
    private String name;
    /**
     * 归属分组
     */
    private String belongTo;

    /**
     * 是否禁用,0-启用，1-禁用
     */
    private Integer forbidden;

    /**
     * 查询级别，0-本级，1-下级，2-本下
     */
    @JsonSetter(nulls = Nulls.SKIP)
    @Range(min = 0, max = 2, message = "查询级别错误")
    private Integer level = 2;

    @JsonIgnore
    private String tenantId;

    @JsonIgnore
    private String groupIdPath;
}
