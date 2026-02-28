package com.hbcy.authcenter.api.modules.core.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.app.dto.ResPermDTO;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.gateway.dto.ApiPermDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author 姚泰然
 * @date 2025-12-24 16:50
 */
@Mapper
public interface ResourcePermMapper extends BaseMapper<ResourcePerm> {
    /**
     * 过滤出有效的权限点id
     *
     * @param appId     应用id
     * @param supplyIds 客户端提供的id
     * @return 有效的id
     */
    Set<String> filterAppPermIds(@Param("appId") String appId, @Param("supplyIds") Set<String> supplyIds);

    /**
     * 列出应用下的权限点id
     *
     * @param appId 应用id
     * @return 权限点id
     */
    Set<String> listAppPermIds(@Param("appId") String appId);

    /**
     * 列出应用下的权限点id
     *
     * @param appIds 应用id
     * @return 权限点id
     */
    Set<String> listAppsPermIds(@Param("appIds") Set<String> appIds);

    /**
     * 列出所有或指定应用的API权限点
     *
     * @return API权限点
     */
    List<ApiPermDTO> select4Gateway(@Param("appId") String appId);

    /**
     * 获取应用所有权限信息
     * @param appId 应用id
     * @return 权限信息
     */
    List<ResPermDTO> listAppPerms(@Param("appId") String appId);

    /**
     * 根据权限点id，获取所有权限点所关联的资源path
     *
     * @param permIds 权限点id
     * @return idPath
     */
    List<String> getPermResIdPaths(@Param("permIds") Collection<String> permIds);

    /**
     * 根据权限码搜索权限点
     * @param appId 应用id
     * @param permCode 权限码
     * @return 权限点id
     */
    List<String> listPermCodeIds(@Param("appId") String appId, @Param("permCode") String permCode);
}