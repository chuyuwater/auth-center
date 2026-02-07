package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import com.hbcy.authcenter.api.modules.intgr.oa.constants.OaConstants;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.hbcy.authcenter.api.modules.intgr.oa.constants.OaConstants.OA_ORG_KEY;

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
    /**
     * 子账号，计算出来的
     */
    private List<OaPersonDTO> subAccounts = new ArrayList<>();

    public String getEmail() {
        if (StringUtils.isBlank(email) || !email.contains("@")) {
            return null;
        }
        return email;
    }

    public boolean isMainAccount() {
        return "0".equals(accounttype);
    }

    //组织节点id
    public String getOrgRelateId() {
        return OA_ORG_KEY.formatted(OaConstants.ORG_TYPE_SUBCOMPANY, subcompanyid1);
    }

    //部门节点id，部门为空则使用组织节点id
    public String getRelateId() {
        if (StringUtils.isNotBlank(departmentid)) {
            return OA_ORG_KEY.formatted(OaConstants.ORG_TYPE_DEPARTMENT, departmentid);
        }
        return getOrgRelateId();
    }
}
