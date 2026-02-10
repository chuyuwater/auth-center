package com.hbcy.authcenter.api.modules.core.user.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 将用户加到组织中
 * 传入叶子结点id即可
 *
 * @author 姚泰然
 * @date 2025-12-26 19:42
 */
@Data
public class UserAddOrgVO {
    /**
     * 用户id
     */
    @NotBlank(message = "用户id不能为空")
    private String userId;
    /**
     * 组织id或部门id
     */
    @NotEmpty(message = "组织id或部门id不能为空")
    private List<String> nodeIds;
}
