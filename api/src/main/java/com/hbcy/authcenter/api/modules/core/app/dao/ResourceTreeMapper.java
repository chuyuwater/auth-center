package com.hbcy.authcenter.api.modules.core.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourceTreeQueryVO;
import com.hbcy.authcenter.api.modules.minor.user.vo.OrderedMenuQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author 姚泰然
 * @date 2025-12-24 15:19
 */
@Mapper
public interface ResourceTreeMapper extends BaseMapper<ResourceTree> {
    /**
     * 权限资源树查询
     *
     * @return 下面所有的节点
     */
    List<ResourceTree> listChildren(@Param("vo") ResourceTreeQueryVO vo);

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

    /**
     * 获取满足条件的idPath
     *
     * @param resIds 叶子节点id
     * @param appIds 应用id
     * @return idPath
     */
    Set<String> listIdPath(@Param("resIds") Set<String> resIds, @Param("appIds") Set<String> appIds);

    /**
     * 获取满足条件的菜单
     *
     * @param vo 查询条件
     * @return 排序好的菜单
     */
    List<ResourceTree> listOrderdMenu(OrderedMenuQueryVO vo);

    /**
     * 获取子节点最大show_order
     *
     * @param appId     应用id
     * @param parentId  父节点id
     * @return 子节点最大show_order
     */
    Integer getMaxChildShowOrder(@Param("appId") String appId, @Param("parentId") String parentId);
}