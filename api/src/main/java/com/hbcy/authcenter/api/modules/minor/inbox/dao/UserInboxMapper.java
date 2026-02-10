package com.hbcy.authcenter.api.modules.minor.inbox.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbcy.authcenter.api.modules.minor.inbox.dto.UserInboxDTO;
import com.hbcy.authcenter.api.modules.minor.inbox.model.UserInbox;
import com.hbcy.authcenter.api.modules.minor.inbox.vo.UserMsgQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-02-10 11:04
 */
@Mapper
public interface UserInboxMapper extends BaseMapper<UserInbox> {
    void insertIgnore(@Param("msgs") List<UserInbox> msgs);

    Page<UserInboxDTO> query(Page<?> dbPage, @Param("vo") UserMsgQueryVO vo);
}