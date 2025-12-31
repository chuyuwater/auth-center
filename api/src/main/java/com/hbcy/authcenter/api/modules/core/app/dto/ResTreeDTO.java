package com.hbcy.authcenter.api.modules.core.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 资源树节点信息DTO
 *
 * @author 姚泰然
 * @date 2025-12-24 15:33
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
public class ResTreeDTO {
    /**
     * 菜单信息
     */
    private ResourceTree res;
    /**
     * 权限信息
     */
    private ResourcePerm perm;
    /**
     * 是否已授权
     * 用于构建授权树
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean granted;
}
