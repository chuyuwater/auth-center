package com.hbcy.authcenter.api.modules.core.user.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 覆盖用户的组织关系
 * @author 姚泰然
 * @date 2026-02-10 11:17
 */
@Data
public class UserOrgOverwriteVO {
    /**
     * 用户id
     */
    @NotBlank
    private String userId;
    /**
     * 组织id或部门id
     */
    private List<String> nodeId;
}
