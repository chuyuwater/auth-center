package com.hbcy.authcenter.api.modules.intgr.oa.dto;

import lombok.Data;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-21 11:31
 */
@Data
public class OaOrgListDTO {
    private Boolean status;
    private String msg;
    private List<OaOrgDTO> datas;
}
