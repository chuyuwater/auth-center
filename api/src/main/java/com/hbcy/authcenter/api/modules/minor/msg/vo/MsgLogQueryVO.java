package com.hbcy.authcenter.api.modules.minor.msg.vo;

import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class MsgLogQueryVO extends PageVO {
    private String keyword;
    private String bizEntity;
    private String msgType;
    private Integer sendStatus;
    private LocalDateTime receiveTimeStart;
    private LocalDateTime receiveTimeEnd;
}
