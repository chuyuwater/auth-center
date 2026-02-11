package com.hbcy.authcenter.api.modules.minor.msg.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbcy.authcenter.api.modules.minor.msg.dto.UserInboxDTO;
import com.hbcy.authcenter.api.modules.minor.msg.model.UserMsg;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-02-10 11:04
 */
@Mapper
public interface UserMsgMapper extends BaseMapper<UserMsg> {
    void insertIgnore(@Param("msgs") List<UserMsg> msgs);

    Page<UserInboxDTO> query(Page<?> dbPage, @Param("vo") UserMsgQueryVO vo);
}