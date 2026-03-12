package com.hbcy.authcenter.api.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户待办查询范围：按接收人（我）或按发起人（我）
 *
 * @author 姚泰然
 * @date 2026-03-12
 */
@Getter
public enum TodoQueryScopeEnum {
    /** 按接收人过滤：target_user = 当前用户（我的待办、我的已办、送阅我的，配合 processState） */
    TARGET_ME("target_me"),
    /** 按发起人过滤：initiator_id = 当前用户（我发起的） */
    INITIATOR_ME("initiator_me");

    @JsonValue
    private final String code;

    TodoQueryScopeEnum(String code) {
        this.code = code;
    }

    @JsonCreator
    public static TodoQueryScopeEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (TodoQueryScopeEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}
