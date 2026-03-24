package com.hbcy.authcenter.api.modules.core.user.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 列表页筛选
 * @author 姚泰然
 * @date 2026-02-11 16:29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserFilterVO extends UserBasicQueryVO {
    /**
     * 是否禁用
     */
    private Integer forbidden;
    /**
     * 指定用户
     */
    private String userId;


    /**
     * 任职是否具体到部门
     */
    @JsonIgnore
    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean deptJob = false;
}
