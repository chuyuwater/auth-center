package com.hbcy.authcenter.api.modules.core.perm.vo;

import com.hbcy.common.db.model.PageVO;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限单元用户查询VO
 *
 * @author 姚泰然
 * @date 2025-12-28 20:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PermUnitUserQueryVO extends PageVO {
    /**
     * 权限单元id
     */
    @NotBlank(message = "权限单元id不能为空")
    private String unitId;
    /**
     * 关键字
     */
    private String keyword;
}
