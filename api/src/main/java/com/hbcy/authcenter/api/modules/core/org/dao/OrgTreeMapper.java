package com.hbcy.authcenter.api.modules.core.org.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.sdk.feign.vo.OrgNodeQueryVO;
import com.hbcy.common.web.api.NamedId;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author 姚泰然
 * @date 2025-12-25 17:47
 */
@Mapper
public interface OrgTreeMapper extends BaseMapper<OrgTree> {
    /**
     * @param tenantId     租户id
     * @param parentIdPath 父节点id
     * @return 下面所有的节点
     */
    List<OrgTree> listChildren(@Param("tenantId") String tenantId,
                               @Param("parentIdPath") String parentIdPath,
                               @Param("vo") OrgNodeQueryVO vo);

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
     * 获取子节点中某一类型的最大id
     *
     * @param parentId 父节点
     */
    String selectMaxId(@Param("parentId") String parentId, @Param("nodeType") Integer nodeType);

    /**
     * 单节点查询名称
     *
     * @param orgId 组织/部门id
     * @return 组织/部门名称
     */
    String selectNameById(@Param("orgId") String orgId);

    /**
     * 多节点查询名称
     *
     * @param orgIds      组织/部门id
     * @param useFullName 全称还是简称
     * @return 组织/部门名称
     */
    List<NamedId> selectNameByIds(@Param("orgIds") Collection<String> orgIds,
                                  @Param("useFullName") boolean useFullName);

    /**
     * 获取子节点最大显示顺序
     *
     * @param parentId 父节点id
     * @return 最大显示顺序
     */
    int getChildMaxShowOrder(@Param("tenantId") String tenantId, @Param("parentId") String parentId);
}