package com.hbcy.authcenter.api.common.enums;

import lombok.Getter;

/**
 * @author 姚泰然
 * @date 2026-02-11 14:17
 */
public enum TreeQueryLevelEnum {
    CURRENT(0, "本级"),
    CHILD(1, "下级"),
    CURRENT_AND_CHILD(2, "本下");

    @Getter
    private final Integer code;
    @Getter
    private final String message;

    TreeQueryLevelEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
