package com.hbcy.authcenter.api.modules.core.perm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnit;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author 姚泰然
 * @date 2025-12-27 20:34
 */
@Mapper
public interface PermUnitMapper extends BaseMapper<PermUnit> {
    Page<PermUnit> listPermUnit(Page<PermUnit> dbPage,
                                @Param("vo") PermUnitQueryVO vo);
}