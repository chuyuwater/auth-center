package com.hbcy.authcenter.api.modules.core.app.dto;

import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePermApi;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 权限点详情（含关联的API列表）
 *
 * @author 姚泰然
 * @date 2026-03-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResourcePermDTO extends ResourcePerm {
    /**
     * 关联的API列表
     */
    private List<ResourcePermApi> apis;
}
