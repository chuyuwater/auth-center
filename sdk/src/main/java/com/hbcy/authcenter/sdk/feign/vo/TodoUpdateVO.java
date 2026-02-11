package com.hbcy.authcenter.sdk.feign.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-27 10:58
 */
@Data
public class TodoUpdateVO {
    /**
     * 处理状态，0-待办，2-已办，4-办结，8-抄送
     */
    @NotNull(message = "处理状态不能为空")
    private Integer processState;
    /**
     * 源应用id
     */
    @NotBlank(message = "源应用id不能为空")
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
