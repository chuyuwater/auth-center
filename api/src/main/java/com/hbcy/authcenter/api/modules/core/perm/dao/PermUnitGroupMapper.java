package com.hbcy.authcenter.api.modules.core.perm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitGroup;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitGroupQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2025-12-28 10:04
 */
@Mapper
public interface PermUnitGroupMapper extends BaseMapper<PermUnitGroup> {

    List<PermUnitGroup> listChildren(@Param("tenantId") String tenantId,
                                     @Param("parentIdPath") String parentIdPath,
                                     @Param("vo") PermUnitGroupQueryVO vo);

    void append(@Param("entity") PermUnitGroup entity);

    void updateIdPath(@Param("tenantId") String tenantId,
                      @Param("oldPath") String oldPath, @Param("newPath") String newPath);

    void updateShowOrder(@Param("tenantId") String tenantId, @Param("parentId") String parentId,
                         @Param("targetIdx") int targetIdx);

    Integer getChildMaxShowOrder(@Param("tenantId") String tenantId, @Param("parentId") String parentId);
}