package com.hbcy.authcenter.api.modules.core.perm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermTree;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermTreeQueryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2025-12-28 10:04
 */
public interface PermTreeMapper extends BaseMapper<PermTree> {

    List<PermTree> listChildren(@Param("tenantId") String tenantId,
                                @Param("parentIdPath") String parentIdPath,
                                @Param("vo") PermTreeQueryVO vo);

    void append(@Param("entity") PermTree entity);

    void updateIdPath(@Param("tenantId") String tenantId,
                      @Param("oldPath") String oldPath, @Param("newPath") String newPath);

    void updateShowOrder(@Param("tenantId") String tenantId, @Param("parentId") String parentId,
                         @Param("targetIdx") int targetIdx);
}