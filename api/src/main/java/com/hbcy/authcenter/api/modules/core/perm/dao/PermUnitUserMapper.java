package com.hbcy.authcenter.api.modules.core.perm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.app.dto.ResPermDTO;
import com.hbcy.authcenter.api.modules.core.perm.dto.UnitUserDTO;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitUser;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUserQueryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author 姚泰然
 * @date 2025-12-27 20:34
 */
public interface PermUnitUserMapper extends BaseMapper<PermUnitUser> {
    //过滤出用户在当前组织下对指定应用的权限点
    List<ResPermDTO> listFilteredPerms(@Param("userId") String userId, @Param("appId") String appId,
                                       @Param("tenantId") String tenantId, @Param("idPath") String idPath);

    //根据permId获取权限点信息
    List<ResPermDTO> listPermInfo(@Param("permIds") Set<String> permIds);

    //当前应用的所有权限
    List<ResPermDTO> listAppPerms(@Param("appId") String appId);

    //添加用户到权限单元
    void insertIgnore(@Param("list") List<PermUnitUser> list);

    //获取角色授权人员信息
    List<UnitUserDTO> listGrantUsers(@Param("vo") PermUnitUserQueryVO vo);
}