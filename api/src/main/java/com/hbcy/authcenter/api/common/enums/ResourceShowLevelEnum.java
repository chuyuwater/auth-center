package com.hbcy.authcenter.api.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hbcy.common.db.convertor.IPairEnum;
import org.jspecify.annotations.Nullable;

/**
 * @author 姚泰然
 * @date 2025-12-30 10:14
 */
public enum ResourceShowLevelEnum implements IPairEnum<Integer> {
    GLOBAL(0, "全局"),
    ORG(1, "组织级"),
    PRJ(2, "项目级");

    private final Integer value;
    private final String desc;

    ResourceShowLevelEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonCreator
    @Nullable
    private static ResourceShowLevelEnum of(Integer value) {
        for (ResourceShowLevelEnum item : ResourceShowLevelEnum.values()) {
            if (item.value.equals(value)) {
                return item;
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
