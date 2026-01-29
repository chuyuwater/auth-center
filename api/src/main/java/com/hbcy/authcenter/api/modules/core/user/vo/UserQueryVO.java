package com.hbcy.authcenter.api.modules.core.user.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hbcy.common.db.model.PageVO;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

import java.util.Set;

/**
 * 搜索用户信息
 * 也可以用于导出
 *
 * @author 姚泰然
 * @date 2025-12-26 21:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryVO extends PageVO {
    /**
     * 本级
     */
    public static final int LEVEL_CURRENT = 0;
    /**
     * 下级
     */
    public static final int LEVEL_FOLLOWER = 1;
    /**
     * 本级及下级
     */
    public static final int LEVEL_ALL = 2;
    /**
     * 左侧组织树选中的节点id
     */
    @NotBlank(message = "节点id不能为空")
    private String nodeId;
    /**
     * 查询级别，0-本级，1-下级，2-本下
     */
    @Range(min = 0, max = 2, message = "查询级别错误")
    private int level = 2;
    /**
     * 姓名、手机号、账号模糊搜索
     * 性能较差，谨慎使用
     * 考虑接入主数据系统搜索
     * 如果明确指定其中任意一项，则keyword不再生效
     */
    private String keyword;
    /**
     * 是否禁用（选人界面固定为0）
     */
    private Integer forbidden;
    /**
     * 指定用户（选人界面不适用）
     */
    private String userId;
    /**
     * 0-兼职，1-主职
     */
    private Integer mainJob;
    /**
     * 组织架构用简称还是全称
     * 默认简称
     */
    private boolean useFullName;
    /**
     * 用工形式，字典EMPLOYEE_TYPE
     */
    private Integer employeeType;

    /**
     * 后端填充
     * NAME-姓名（如果keyword既没有数字也没有字母和@）
     * PHONE-手机号（只有数字时）
     * ACCOUNT-账号（仅包含数字、字母时）
     */
    @JsonIgnore
    private Set<String> keywordType;
    /**
     * 后端填充，组织前缀匹配
     */
    @JsonIgnore
    private String idPathPrefix;
    /**
     * 租户id，后端填充
     */
    @JsonIgnore
    private String tenantId;
}
