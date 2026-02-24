package com.hbcy.authcenter.api.modules.core.perm.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 权限单元用户更新VO
 *
 * @author 姚泰然
 * @date 2025-12-28
 */
@Data
public class PermUnitUserUpdateVO {
    @NotEmpty(message = "权限单元不能为空")
    private List<String> unitIdList;
    @Valid
    @NotEmpty(message = "至少传入一组数据")
    private List<PermUserGrantVO> userList;
}
