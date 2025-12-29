package com.hbcy.authcenter.api.modules.core.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.user.dto.UserOrgDTO;
import com.hbcy.authcenter.api.modules.core.user.model.UserOrg;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author 姚泰然
 * @date 2025-12-26 08:45
 */
public interface UserOrgMapper extends BaseMapper<UserOrg> {
    /**
     * 获取用户的任职信息
     *
     * @param userIds  用户id
     * @param onlyMain 仅需要主职单位
     * @return 用户任职信息
     */
    List<UserOrgDTO> listUserOrgs(@Param("userIds") Collection<String> userIds,
                                  @Param("onlyMain") boolean onlyMain);

    /**
     * 设置用户的主要任职
     *
     * @param userId 用户id
     * @param orgId  组织id
     */
    void updateMainJob(@Param("userId") String userId, @Param("orgId") String orgId);

    /**
     * 添加用户兼职任职
     *
     * @param userId 用户id
     * @param orgId  组织id
     */
    void updatePartJob(@Param("userId") String userId, @Param("orgId") String orgId);
}