package com.hbcy.authcenter.api.modules.minor.msg.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgChannel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息渠道 Mapper
 */
@Mapper
public interface MsgChannelMapper extends BaseMapper<MsgChannel> {
}
