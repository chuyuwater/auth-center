package com.hbcy.authcenter.api.modules.core.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbcy.authcenter.api.modules.core.user.dto.OrgUserDTO;
import com.hbcy.authcenter.api.modules.core.user.dto.UserQueryResultDTO;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.core.user.vo.UserQueryVO;
import com.hbcy.common.web.api.NamedId;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author 姚泰然
 * @date 2025-12-26 08:54
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<NamedId> selectNameByIds(@Param("uids") Set<String> uids);

    String selectNameById(@Param("userId") String userId);

    Page<UserQueryResultDTO> queryUser(Page<?> dbPage, @Param("vo") UserQueryVO vo);

    Page<OrgUserDTO> filterUser4Select(Page<?> page, @Param("vo") UserQueryVO vo);

    void insertIgnore(@Param("list") List<User> toInsert);
}