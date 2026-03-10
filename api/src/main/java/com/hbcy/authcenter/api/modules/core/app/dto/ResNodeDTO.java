package com.hbcy.authcenter.api.modules.core.app.dto;

import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 单个资源节点详情DTO（含关联权限点及API列表）
 *
 * @author 姚泰然
 * @date 2026-03-10
 */
@Data
@Accessors(chain = true)
public class ResNodeDTO {
    /**
     * 菜单信息
     */
    private ResourceTree res;
    /**
     * 关联的权限点列表（每个权限点含其API列表）
     */
    private List<ResourcePermDTO> perms;
}
