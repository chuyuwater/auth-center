package com.hbcy.authcenter.api.modules.intgr.oa.vo;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-02-09 17:53
 */
@Data
public class OaMsgVO {
    /**
     * 消息标题
     */
    private String title;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 关联流程的链接
     */
    private String pcUrl;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 通信密钥
     */
    private String ak;
}
