package com.hbcy.authcenter.api.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hbcy.common.db.convertor.IPairEnum;
import org.jspecify.annotations.Nullable;

/**
 * @author 姚泰然
 * @date 2025-12-25 12:47
 */
public enum OrgNodeCategoryEnum implements IPairEnum<Integer> {
    PROJECT(0, "项目部"),
    COMPANY(1, "公司"),
    SUB_COMPANY(2, "子公司"),
    BRANCH_COMPANY(3, "分公司");

    private final Integer value;
    private final String desc;

    OrgNodeCategoryEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonCreator
    @Nullable
    public static OrgNodeCategoryEnum of(Integer v) {
        for (OrgNodeCategoryEnum e : values()) {
            if (e.getValue().equals(v)) {
                return e;
            }
        }
        return null;
    }


    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
