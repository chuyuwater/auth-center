package com.hbcy.authcenter.api.modules.minor.msg.vo;

import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模板列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TemplateQueryVO extends PageVO {
    private String keyword;
    private String msgType;
    private String groupId;
}
