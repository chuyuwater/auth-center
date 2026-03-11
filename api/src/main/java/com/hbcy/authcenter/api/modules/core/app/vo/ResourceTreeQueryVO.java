package com.hbcy.authcenter.api.modules.core.app.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hbcy.authcenter.api.common.enums.TreeQueryLevelEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

/**
 * 资源树查询VO
 *
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
    private String parentId;
    /**
     * 父节点ID工作类型
     * 1-下级，2-本下
     */
    @Range(min = 1, max = 2, message = "父节点ID工作类型错误")
    private Integer parentLevel = TreeQueryLevelEnum.CHILD.getCode();
    /**
     * 是否附带权限点信息，默认true
     */
    private Boolean withPerm = true;
    /**
     * 是否附带权限点对应的api信息，默认true
     */
    private Boolean withApi = true;
    /**
     * 0-全端，1-pc端，2-移动端
     */
    private Integer clientType;
    /**
     * 资源类型，0-菜单，1-按钮
     */
    private Integer resType;
    /**
     * 显示级别，0-全局，1-组织级，2-项目级
     */
    private Integer showLevel;
    /**
     * 是否隐藏菜单，0-显示，1-隐藏
     */
    private Integer hidden;
    /**
     * 是否需要创建者信息
     * api侧默认需要
     *
     */
    @JsonIgnore
    private Boolean withCreator = false;
    /**
     * 父节点idPath,后端填充
     */
    @JsonIgnore
    private String idPath;
}
