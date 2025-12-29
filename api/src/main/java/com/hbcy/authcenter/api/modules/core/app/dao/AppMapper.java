package com.hbcy.authcenter.api.modules.core.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.core.app.model.App;
import org.apache.ibatis.annotations.Param;

/**
 * @author 姚泰然
 * @date 2025-12-23 17:10
 */
public interface AppMapper extends BaseMapper<App> {
    void append(@Param("app") App app);

    void move(@Param("targetIdx") int targetIdx);
}