package com.hbcy.authcenter.api.modules.minor.todo.vo;

import com.hbcy.common.db.dictvalue.DictValid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-27
 */
@Data
public class UserTodoUpdateVO {
    @NotNull(message = "处理状态不能为空")
    @DictValid(dictKey = "TODO_PROCESS_STATE", message = "处理状态错误")
    private Integer processState;
    /**
     * 源系统
     */
    private String srcApp;
    /**
     * 源系统id
     */
    @NotBlank(message = "源待办id不能为空")
    private String srcId;

    /**
     * 更新了待办状态的用户
     */
    @NotEmpty(message = "用户id不能为空")
    private List<String> userIds;
}
