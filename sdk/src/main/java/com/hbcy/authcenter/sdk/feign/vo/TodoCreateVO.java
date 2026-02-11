package com.hbcy.authcenter.sdk.feign.vo;

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
public class TodoCreateVO {
    /**
     * 源应用id
     */
    @NotBlank(message = "源应用id不能为空")
    private String srcApp;
    /**
     * 源待办id，用来去重
     */
    @NotBlank(message = "源待办id不能为空")
    private String srcId;
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
     * 处理状态，0-待办，2-已办，4-办结，8-抄送
     */
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
}
