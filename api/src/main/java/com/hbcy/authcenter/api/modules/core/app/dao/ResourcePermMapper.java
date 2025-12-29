package com.hbcy.authcenter.api.modules.core.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author 姚泰然
 * @date 2025-12-24 16:50
 */
public interface ResourcePermMapper extends BaseMapper<ResourcePerm> {
    /**
     * 过滤出有效的权限点id
     *
     * @param appId     应用id
     * @param supplyIds 客户端提供的id
     * @return 有效的id
     */
    Set<String> filterAppPermIds(@Param("appId") String appId, @Param("supplyIds") Set<String> supplyIds);

    /**
     * 列出应用下的权限点id
     *
     * @param appId 应用id
     * @return 权限点id
     */
    Set<String> listAppPermIds(@Param("appId") String appId);
}