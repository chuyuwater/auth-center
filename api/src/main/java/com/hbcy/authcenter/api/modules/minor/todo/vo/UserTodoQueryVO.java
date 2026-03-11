package com.hbcy.authcenter.api.modules.minor.todo.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 待办搜索
 *
 * @author 姚泰然
 * @date 2026-01-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserTodoQueryVO extends PageVO {
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
     * 接收人id
     */
    private String userId;
    /**
     * 搜索者本下组织，服务端填充
     */
    @JsonIgnore
    private Collection<String> searchOrgIds;
}
