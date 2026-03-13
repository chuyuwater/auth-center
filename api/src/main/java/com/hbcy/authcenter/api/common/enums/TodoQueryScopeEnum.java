package com.hbcy.authcenter.api.common.enums;

/**
 * 用户待办查询范围：按接收人（我）或按发起人（我）
 * 前端传 TARGET_ME / INITIATOR_ME，使用 Enum.valueOf 转换
 *
 * @author 姚泰然
 * @date 2026-03-12
 */
public enum TodoQueryScopeEnum {
    /** 按接收人过滤：target_user = 当前用户（我的待办、我的已办、送阅我的，配合 processState） */
    TARGET_ME,
    /** 按发起人过滤：initiator_id = 当前用户（我发起的） */
    INITIATOR_ME
}
