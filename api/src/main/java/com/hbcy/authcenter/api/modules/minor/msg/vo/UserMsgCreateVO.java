package com.hbcy.authcenter.api.modules.minor.msg.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 发送用户消息
 * @author 姚泰然
 * @date 2026-01-27 08:57
 */
@Data
public class UserMsgCreateVO {
    /**
     * 消息源应用
     */
    @NotBlank(message = "源应用id不能为空")
    private String srcApp;
    /**
     * 消息源id
     */
    @NotBlank(message = "源消息id不能为空")
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
     * 消息类型：0-普通消息，1-预警消息
     */
    private Integer type = 0;
    /**
     * 消息产生时间
     */
    private LocalDateTime createTime;
    /**
     * 目标用户
     */
    @NotEmpty(message = "目标用户不能为空")
    private Set<String> targetUsers;
    /**
     * 原始报文，用于调试，不给用户展示，可以传入任意json字符串
     */
    private String originJson;
}
