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
     * @param appId        应用id
     * @param parentIdPath 父节点id
     * @return 下面所有的节点
     */
    List<ResourceTree> listChildrenRecursively(@Param("appId") String appId,
                                               @Param("parentIdPath") String parentIdPath);

    /**
     * 在同级尾部创建数据
     *
     * @param entity 菜单资源
     */
    void append(@Param("entity") ResourceTree entity);

    /**
     * 批量更新节点id_path
     *
     * @param appId   应用id
     * @param oldPath 旧的id_path
     * @param newPath 新的id_path
     */
    void updateIdPath(@Param("appId") String appId,
                      @Param("oldPath") String oldPath, @Param("newPath") String newPath);

    /**
     * 批量更新节点show_order
     *
     * @param appId     应用id
     * @param parentId  父节点id
     * @param targetIdx 目标顺序
     */
    void updateShowOrder(@Param("appId") String appId, @Param("parentId") String parentId,
                         @Param("targetIdx") int targetIdx);
}