package com.hbcy.authcenter.api.modules.minor.todo.vo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-27
 */
@Data
public class UserTodoBatchOpVO {
    @NotEmpty(message = "todoIds不能为空")
    private List<String> todoIds;
}
