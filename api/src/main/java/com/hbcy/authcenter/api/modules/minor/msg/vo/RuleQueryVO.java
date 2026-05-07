package com.hbcy.authcenter.api.modules.minor.msg.vo;

import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RuleQueryVO extends PageVO {
    private String keyword;
    private String bizEntity;
    private String groupId;
}
