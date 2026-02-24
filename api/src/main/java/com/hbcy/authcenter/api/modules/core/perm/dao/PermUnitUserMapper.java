package com.hbcy.authcenter.api.modules.core.perm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbcy.authcenter.api.modules.core.app.dto.GrantAppDTO;
import com.hbcy.authcenter.api.modules.core.app.dto.ResPermDTO;
import com.hbcy.authcenter.api.modules.core.perm.dto.UnitUserDTO;
import com.hbcy.authcenter.api.modules.core.perm.dto.UserUnitDTO;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitUser;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserUpdateVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUserUnitQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author 姚泰然
 * @date 2025-12-27 20:34
 */
@Mapper
public interface PermUnitUserMapper extends BaseMapper<PermUnitUser> {
    //过滤出用户在当前组织下对指定应用的权限点
    List<ResPermDTO> listFilteredPerms(@Param("userId") String userId, @Param("appId") String appId,
                                       @Param("tenantId") String tenantId, @Param("orgIdPath") String orgIdPath);

    //根据permId获取权限点信息
    List<ResPermDTO> listPermInfo(@Param("permIds") Set<String> permIds);

    //添加用户到权限单元
    void insertIgnore(@Param("list") List<PermUnitUser> list);

    //获取角色授权人员信息
    Page<UnitUserDTO> listGrantUsers(Page<?> dbPage, @Param("vo") PermUnitUserQueryVO vo);

    //获取当前用户授权的应用
    List<GrantAppDTO> listGrantApps(@Param("userId") String userId,
                                    @Param("orgId") String orgId,
                                    @Param("withForbidden") boolean withForbidden);

    //获取用户在指定组织下的所有权限点
    Set<String> listUserPerms(@Param("userId") String userId, @Param("orgIdPath") String orgIdPath);

    //获取用户在指定组织下的所有菜单资源
    Set<String> listUserRes(@Param("userId") String userId,
                            @Param("orgIdPath") String orgIdPath);

    //判断用户是否有某个权限
    int hasPerm(@Param("userId") String userId, @Param("orgId") String orgId,
                @Param("permIds") Set<String> permIds);

    //按用户身份移除授权
    void batchDelete(@Param("vo") PermUnitUserUpdateVO vo, @Param("tenantId") String tenantId);

    //获取用户已授权的权限单元
    List<UserUnitDTO> listUserUnits(@Param("vo") PermUserUnitQueryVO vo);
}