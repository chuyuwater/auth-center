package com.hbcy.authcenter.api.common.enums;

import lombok.Getter;

/**
 * @author 姚泰然
 * @date 2026-03-12 08:22
 */
public enum TodoProcessStateEnum {
    TODO(0, "待办"),
    DONE(2, "已办"),
    FINISHED(4, "办结"),
    CC(8, "抄送");
    @Getter
    private final int value;
    @Getter
    private final String desc;

    TodoProcessStateEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
