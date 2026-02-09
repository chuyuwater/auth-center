package com.hbcy.authcenter.api.modules.intgr.oa.vo;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-02-09 17:53
 */
@Data
public class OaMsgVO {
    private String title;
    private String userId;
    private String appUrl;
    private String pcUrl;
    private String content;
}
