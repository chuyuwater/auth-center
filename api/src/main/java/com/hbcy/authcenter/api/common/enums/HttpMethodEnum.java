package com.hbcy.authcenter.api.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hbcy.common.db.convertor.IPairEnum;
import org.jspecify.annotations.Nullable;

/**
 * @author 姚泰然
 * @date 2025-12-30 12:47
 */
public enum HttpMethodEnum implements IPairEnum<Integer> {
    GET(0),
    POST(1),
    PUT(2),
    DELETE(3);

    private final Integer value;

    HttpMethodEnum(Integer value) {
        this.value = value;
    }

    @JsonCreator
    @Nullable
    public static HttpMethodEnum of(Integer value) {
        for (HttpMethodEnum item : values()) {
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
        return this.name();
    }
}
