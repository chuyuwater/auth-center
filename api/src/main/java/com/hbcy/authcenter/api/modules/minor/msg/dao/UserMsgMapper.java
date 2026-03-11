package com.hbcy.authcenter.api.modules.minor.msg.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgDTO;
import com.hbcy.authcenter.api.modules.minor.msg.dto.UserMsgDTO;
import com.hbcy.authcenter.api.modules.minor.msg.model.UserMsg;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-02-10 11:04
 */
@Mapper
public interface UserMsgMapper extends BaseMapper<UserMsg> {
    void insertIgnore(@Param("msgs") List<UserMsg> msgs);

    Page<UserMsgDTO> query4User(Page<?> dbPage, @Param("vo") UserMsgQueryVO vo);

    Page<MsgDTO> listMsg(Page<?> dbPage, @Param("vo") UserMsgQueryVO vo);

    /**
     * 根据 id 查询消息（仅当消息接收人属于指定组织范围内时返回）
     */
    MsgDTO getMsgByIdInOrgs(@Param("id") String id, @Param("orgIds") Collection<String> orgIds);

    /**
     * 查询在指定组织范围内有推送消息的应用 id 列表（去重）
     */
    List<String> listDistinctSrcAppInOrgs(@Param("orgIds") Collection<String> orgIds);
}