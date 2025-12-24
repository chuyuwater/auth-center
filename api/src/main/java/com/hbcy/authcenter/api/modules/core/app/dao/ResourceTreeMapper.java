package com.hbcy.authcenter.api.modules.core.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2025-12-24 15:19
 */
public interface ResourceTreeMapper extends BaseMapper<ResourceTree> {
    /**
     * @param appId    应用id
     * @param parentId 父节点id
     * @return 下面所有的节点（不含父节点）
     */
    List<ResourceTree> listChildrenRecursively(@Param("appId") String appId,
                                               @Param("parentId") String parentId);
}