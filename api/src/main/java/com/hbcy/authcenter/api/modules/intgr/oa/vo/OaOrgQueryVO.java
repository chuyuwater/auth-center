package com.hbcy.authcenter.api.modules.intgr.oa.vo;

import feign.form.FormProperty;
import lombok.Data;

/**
 * 查询OA的组织结构
 * @author 姚泰然
 * @date 2026-01-21 09:51
 */
@Data
public class OaOrgQueryVO {
    /**
     * 父节点组织类型
     */
    private String type;
    /**
     * 父节点id
     */
    private String id;
    /**
     * 父节点是不是virtual
     */
    private int isVirtual = 0;
    private int virtualCompanyid = 1;
    private int isLoadSubDepartment = 1;
    /**
     * 时间戳参数
     */
    @FormProperty("__random__")
    private Long random;
}
