package com.hbcy.authcenter.api.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hbcy.common.db.convertor.IPairEnum;
import org.jspecify.annotations.Nullable;

/**
 * @author 姚泰然
 * @date 2025-12-25 12:41
 */
public enum OrgNodeTypeEnum implements IPairEnum<Integer> {
    ORG(0, "组织"),
    DEPT(1, "部门");
    private final Integer value;
    private final String desc;

    OrgNodeTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonCreator
    @Nullable
    private static OrgNodeTypeEnum of(Integer value) {
        for (OrgNodeTypeEnum type : values()) {
            if (type.getValue().equals(value)) {
                return type;
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
