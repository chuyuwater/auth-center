package com.hbcy.authcenter.api.modules.core.perm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitResource;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author 姚泰然
 * @date 2025-12-27 20:34
 */
public interface PermUnitResourceMapper extends BaseMapper<PermUnitResource> {
    Set<String> listGrantedApps(@Param("unitId") String unitId);
}