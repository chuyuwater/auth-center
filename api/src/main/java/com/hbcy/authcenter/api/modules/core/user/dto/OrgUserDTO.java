package com.hbcy.authcenter.api.modules.core.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 组织人员
 * 选人窗口使用
 *
 * @author 姚泰然
 * @date 2025-12-29 13:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OrgUserDTO extends UserDTO {
    /**
     * 节点
     */
    private String nodeId;
    /**
     * 节点全路径
     */
    private String idPath;
    /**
     * 节点全路径（名字，反转）
     */
    private String namePath;
    /**
     * 0-兼职，1-主职
     */
    private Integer mainJob;
}
