package com.hbcy.authcenter.api.modules.minor.todo.dto;

import com.hbcy.common.db.dictvalue.DictInject;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2026-01-27
 */
@Data
public class UserTodoDTO {
    /**
     * 待办ID
     */
    private String id;

    /**
     * 来源应用
     */
    private String srcApp;

    /**
     * 待办标题
     */
    private String todoTitle;

    /**
     * 待办内容
     */
    private String todoContent;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 处理状态
     */
    @DictInject(value = "TODO_PROCESS_STATE")
    private Integer processState;

    /**
     * 待办查看状态, 0-未读，1-已读
     */
    private Integer viewState;

    /**
     * 待办类型
     */
    private Integer todoType;

    /**
     * 关联链接
     */
    private String relateLink;
}
