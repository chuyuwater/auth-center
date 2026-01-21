package com.hbcy.authcenter.api.modules.intgr.oa.vo;

import lombok.Data;

import java.util.Map;

/**
 * 查询待办条件
 * @author 姚泰然
 * @date 2026-01-21 12:07
 */
@Data
public class OaQueryTodoVO {
    private Map<String, String> conditions;
    /**
     *是否主次账号统一显示
     */
    private Boolean isMergeShow;
    /**
     * 是否显示异构系统数据 ， 为true时还需 设置开启异构系统显示才会显示
     */
    private Boolean isNeedOs;
    /**
     *分页页码
     */
    private Long pageNo;
    /**
     *页内数量
     */
    private Long pageSize;
}
