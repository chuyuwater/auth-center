package com.hbcy.authcenter.api.modules.minor.msg.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息日志 Mapper
 */
@Mapper
public interface MsgLogMapper extends BaseMapper<MsgLog> {

    /**
     * 查询需要重试的消息（发送失败且未超过最大重试次数）
     */
    @Select("SELECT * FROM msg_log WHERE send_status = 2 AND retry_count < #{maxRetryCount} AND TIMESTAMPDIFF(SECOND, update_time, NOW()) >= #{retryInterval} LIMIT 100")
    List<MsgLog> selectRetryPending(@Param("maxRetryCount") int maxRetryCount, @Param("retryInterval") int retryInterval);
}
