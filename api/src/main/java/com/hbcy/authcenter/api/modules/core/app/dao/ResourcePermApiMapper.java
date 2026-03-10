package com.hbcy.authcenter.api.modules.core.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePermApi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * @author 姚泰然
 * @date 2026-03-10 08:46
 */
@Mapper
public interface ResourcePermApiMapper extends BaseMapper<ResourcePermApi> {
    /**
     * 根据权限点id批量删除api配置
     *
     * @param permIds 权限点id列表
     */
    void deleteByPermIds(@Param("permIds") Collection<String> permIds);
}