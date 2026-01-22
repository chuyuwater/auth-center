package com.hbcy.authcenter.api.modules.intgr.oa.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 修改session分页大小
 * @author 姚泰然
 * @date 2026-01-21 09:47
 */
@Data
@Accessors(chain = true)
public class OaPageSizeModifyVO {
    private String dataKey;
    private long pageSize;
}
