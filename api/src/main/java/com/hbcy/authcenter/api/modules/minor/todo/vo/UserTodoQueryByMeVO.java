package com.hbcy.authcenter.api.modules.minor.todo.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hbcy.authcenter.api.common.enums.TodoQueryScopeEnum;
import com.hbcy.common.db.model.PageVO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 待办搜索：用于首页个人中心
 *
 * @author 姚泰然
 * @date 2026-01-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserTodoQueryByMeVO extends PageVO {
    /**
     * 关键词（匹配待办标题或待办内容）
     */
    private String keyword;
    /**
     * 处理状态
     */
    private Integer processState;
    /**
     * 查看状态：0-未读，1-已读
     */
    private Integer viewState;
    /**
     * 发送时间起始
     */
    private LocalDateTime sendTimeStart;
    /**
     * 发送时间结束
     */
    private LocalDateTime sendTimeEnd;
    /**
     * 待办类型：0-流程待办，1-任务待办
     */
    private Integer todoType;
    /**
     * 来源应用 id 列表（多选，已推送待办的应用）
     */
    private List<String> srcApp;
    /**
     * 查询范围：TARGET_ME-按接收人（我），INITIATOR_ME-按发起人（我）；后端据此填充 userId 或 initiatorId
     */
    @NotNull(message = "必须选择查询范围")
    private TodoQueryScopeEnum scope;
    /**
     * 发起人 id（服务端根据 scope=INITIATOR_ME 填充）
     */
    @JsonIgnore
    private String initiatorId;
    /**
     * 接收人 id（服务端根据 scope=TARGET_ME 填充）
     */
    @JsonIgnore
    private String userId;
    /**
     * 搜索者本下组织，服务端填充
     */
    @JsonIgnore
    private Collection<String> searchOrgIds;
}
