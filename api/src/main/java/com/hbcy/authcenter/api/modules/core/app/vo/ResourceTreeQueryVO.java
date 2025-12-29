package com.hbcy.authcenter.api.modules.core.app.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 姚泰然
 * @date 2025-12-24
 */
@Data
@Accessors(chain = true)
public class ResourceTreeQueryVO {
    /**
     * 应用ID
     */
    @NotBlank(message = "应用ID不能为空")
    private String appId;
    /**
     * 关键字
     */
    private String keyword;
    /**
     * 父节点ID
     */
    private String parentId = "";
    /**
     * 是否查询权限资源
     */
    private boolean withPerm = true;
    /**
     * 0-全端，1-pc端，2-移动端
     */
    private Integer clientType;
}
