package com.hbcy.authcenter.api.modules.core.user.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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
    @NotBlank
    private String userId;
    /**
     * 组织id或部门id
     */
    @NotBlank
    private String nodeId;
}
