package com.hbcy.authcenter.api.modules.core.user.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hbcy.common.db.model.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
     * 指定组织
     */
    private String orgId;
    /**
     * 查询级别，0-本级，1-下级，2-本下
     */
    private int level = 2;
    /**
     * 姓名、手机号、邮箱、账号模糊搜索
     * 性能较差，谨慎使用
     * 考虑接入主数据系统搜索
     * 如果明确指定其中任意一项，则keyword不再生效
     */
    private String keyword;
    /**
     * 后端填充
     * NAME-姓名（如果keyword既没有数字也没有字母和@）
     * PHONE-手机号（只有数字时）
     * EMAIL-邮箱（包含@时，字母或数字时）
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
    /**
     * 姓名模糊
     */
    private String name;
    /**
     * 手机号模糊
     */
    private String phone;
    /**
     * 邮箱模糊
     */
    private String email;
    /**
     * 账号模糊
     */
    private String account;
    /**
     * 是否禁用
     */
    private Integer forbidden;
    /**
     * 指定用户
     */
    private String userId;
}
