package com.hbcy.authcenter.api.modules.core.perm.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PermUnitUserUpdateVO {
    @NotBlank(message = "权限单元不能为空")
    private String unitId;
    @Valid
    @NotEmpty(message = "至少传入一组数据")
    private List<PermUserGrantVO> userList;
}
