package com.hbcy.authcenter.api.modules.minor.todo.dto;

import com.hbcy.common.db.dictvalue.DictInject;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2026-03-11 10:47
 */
@Data
public class TodoDTO {
    private String id;
    /**
     * 源app id
     */
    private String srcApp;
    /**
     * 源app名称
     */
    private String srcAppName;
    /**
     * 源id，用于去重
     */
    private String srcId;
    /**
     * 待办标题
     */
    private String todoTitle;
    /**
     * 待办内容
     */
    private String todoContent;
    /**
     * 接收者
     */
    private String targetUser;
    /**
     * 接收者名称
     */
    private String targetUserName;
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    /**
     * 处理状态，字典TODO_PROCESS_STATE
     */
    @DictInject("TODO_PROCESS_STATE")
    private Integer processState;
    /**
     * 0-未读，1-已读
     */
    private Integer viewState;
    /**
     * 待办类型，0-流程待办，1-任务待办
     */
    private Integer todoType;
    /**
     * 关联链接
     */
    private String relateLink;
    /**
     * 原始报文
     */
    private String originJson;
    /**
     * 接收时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
