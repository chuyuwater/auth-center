package com.hbcy.authcenter.api.modules.minor.msg.vo;

import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SchemeQueryVO extends PageVO {
    private String keyword;
    private String groupId;
    private List<String> bizTypes;
}
