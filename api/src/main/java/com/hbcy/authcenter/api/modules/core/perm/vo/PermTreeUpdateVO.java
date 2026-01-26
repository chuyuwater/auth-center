package com.hbcy.authcenter.api.modules.core.perm.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 角色分组更新VO
 *
 * @author 姚泰然
 * @date 2025-12-28
 */
@Data
public class PermTreeUpdateVO {
    /**
     * 节点名称
     */
    @NotBlank(message = "名称不能为空")
    @Length(max = 50, message = "名称长度不能超过50")
    private String nodeName;

    /**
     * 备注
     */
    @Length(max = 200, message = "说明长度不能超过200")
    private String memo = "";
    /**
     * 父节点。
     * 创建时，节点固定在同级末尾。
     */
    private String parentId = "";
}
