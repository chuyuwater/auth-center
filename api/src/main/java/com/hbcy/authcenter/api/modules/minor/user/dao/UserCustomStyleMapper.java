package com.hbcy.authcenter.api.modules.minor.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.minor.user.model.UserCustomStyle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author 姚泰然
 * @date 2026-04-03 14:40
 */
@Mapper
public interface UserCustomStyleMapper extends BaseMapper<UserCustomStyle> {
    void upsert(@Param("entity") UserCustomStyle entity);
}