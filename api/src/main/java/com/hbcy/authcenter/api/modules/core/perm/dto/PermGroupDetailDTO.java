package com.hbcy.authcenter.api.modules.core.perm.dto;

import com.hbcy.authcenter.api.modules.core.perm.model.PermTree;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 姚泰然
 * @date 2026-02-11 10:24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PermGroupDetailDTO extends PermTree {
    private String parentName;
}
