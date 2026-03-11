package com.hbcy.authcenter.api.modules.minor.msg.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * 消息搜索
 * @author 姚泰然
 * @date 2026-01-27 09:03
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserMsgQueryVO extends PageVO {
    /**
     * 关键词
     */
    private String keyword;
    /**
     * 查看状态, 0-未读，1-已读
     */
    private Integer viewStatus;
    /**
     * 发送时间起始
     */
    private LocalDateTime sendTimeStart;
    /**
     * 发送时间结束
     */
    private LocalDateTime sendTimeEnd;
    /**
     * 消息类型
     */
    private Integer msgType;
    /**
     * 消息来源应用
     */
    private String srcApp;
    /**
     * 用户id
     * 个人用户只能搜自己的，无需填充
     */
    private String userId;

    @JsonIgnore
    private String tenantId;
    /**
     * 搜索者本下组织
     */
    @JsonIgnore
    private Collection<String> searchOrgIds;
}
