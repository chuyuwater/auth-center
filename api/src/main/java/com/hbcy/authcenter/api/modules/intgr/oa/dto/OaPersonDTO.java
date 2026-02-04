package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-01-21 11:11
 */
@Data
public class OaPersonDTO {
    /**
     * 人员id
     */
    private String id;
    /**
     * 主账号
     */
    private String belongto;
    /**
     * 姓名
     */
    private String lastname;
    /**
     * 状态
     */
    private String status;
    /**
     * 账号类型
     * 0-主职，1-兼职
     */
    private String accounttype;
    /**
     * email
     */
    private String email;
    /**
     * 性别
     */
    private String sex;
    /**
     * 部门id
     */
    private String departmentid;
    /**
     * 分部id
     */
    private String subcompanyid1;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 排序
     */
    private String dsporder;
}
