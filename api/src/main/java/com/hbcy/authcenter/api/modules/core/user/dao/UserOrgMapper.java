package com.hbcy.authcenter.api.modules.core.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.user.dto.UserOrgDTO;
import com.hbcy.authcenter.api.modules.core.user.model.UserOrg;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2025-12-26 08:45
 */
public interface UserOrgMapper extends BaseMapper<UserOrg> {
    List<UserOrgDTO> listUserOrgs(@Param("userIds") List<String> userIds);
}