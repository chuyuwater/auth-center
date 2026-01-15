package com.hbcy.authcenter.api.modules.sys.dict.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.sys.dict.model.SysDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-14 17:02
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {
    List<SysDict> selectChildrenRecursively(@Param("parentId") String parentId);
}