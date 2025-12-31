package com.hbcy.authcenter.api.modules.core.perm.vo;

import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限单元查询VO
 *
 * @author 姚泰然
 * @date 2025-12-28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PermUnitQueryVO extends PageVO {
    /**
     * 名称模糊查询
     */
    private String name;
    /**
     * 归属分组
     */
    private String belongTo;
}
