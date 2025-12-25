package com.hbcy.authcenter.api.modules.core.org.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.vo.OrgTreeQueryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2025-12-25 15:40
 */
public interface OrgTreeMapper extends BaseMapper<OrgTree> {
    /**
     * @param tenantId     租户id
     * @param parentIdPath 父节点id
     * @return 下面所有的节点
     */
    List<OrgTree> listChildrenRecursively(@Param("tenantId") String tenantId,
                                          @Param("parentIdPath") String parentIdPath,
                                          @Param("vo") OrgTreeQueryVO vo);

    /**
     * 在同级尾部创建数据
     *
     * @param entity 组织树节点
     */
    void append(@Param("entity") OrgTree entity);

    /**
     * 批量更新节点id_path
     *
     * @param tenantId 租户id
     * @param oldPath  旧的id_path
     * @param newPath  新的id_path
     */
    void updateIdPath(@Param("tenantId") String tenantId,
                      @Param("oldPath") String oldPath, @Param("newPath") String newPath);

    /**
     * 批量更新节点show_order
     *
     * @param tenantId  租户id
     * @param parentId  父节点id
     * @param targetIdx 目标顺序
     */
    void updateShowOrder(@Param("tenantId") String tenantId, @Param("parentId") String parentId,
                         @Param("targetIdx") int targetIdx);

    /**
     * 获取子节点最大id
     *
     * @param parentId 父节点
     */
    String selectMaxId(@Param("parentId") String parentId);
}