package com.hbcy.authcenter.api.modules.minor.msg.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgSchemeChannel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发送方案渠道关联 Mapper
 */
@Mapper
public interface MsgSchemeChannelMapper extends BaseMapper<MsgSchemeChannel> {
}
