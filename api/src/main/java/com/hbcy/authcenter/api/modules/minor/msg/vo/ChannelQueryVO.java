package com.hbcy.authcenter.api.modules.minor.msg.vo;

import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 渠道列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChannelQueryVO extends PageVO {
    /** 渠道名称/编号模糊检索 */
    private String keyword;
    /** 渠道类型筛选 */
    private String channelType;
}
