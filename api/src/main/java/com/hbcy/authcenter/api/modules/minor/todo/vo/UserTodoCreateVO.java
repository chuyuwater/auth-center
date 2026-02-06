package com.hbcy.authcenter.api.modules.minor.todo.vo;

import com.hbcy.common.db.dictvalue.DictValid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 创建待办
 * @author 姚泰然
 * @date 2026-01-27
 */
@Data
public class UserTodoCreateVO {
    /**
     * 源应用id
     */
    @NotBlank(message = "源应用id不能为空")
    private String srcApp;
    /**
     * 源待办id
     */
    @NotBlank(message = "源待办id不能为空")
    private String srcId;
    /**
     * 源系统用户id
     */
    private String srcUser;
    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 链接
     */
    private String link;
    /**
     * 待办类型：0-流程待办，1-任务待办
     */
    private int type;
    /**
     * 处理状态
     */
    @DictValid(dictKey = "TODO_PROCESS_STATE", message = "处理状态错误")
    @NotNull(message = "处理状态不能为空")
    private Integer processState;
    /**
     * 待办产生时间
     */
    private LocalDateTime createTime;
    /**
     * 目标用户
     */
    @NotEmpty(message = "目标用户不能为空")
    private Set<String> targetUsers;
    /**
     * 原始报文，用于调试
     */
    private String originJson;
}
