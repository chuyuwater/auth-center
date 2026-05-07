package com.hbcy.authcenter.api.modules.minor.msg.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.SelectRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 选人规则 Mapper
 */
@Mapper
public interface SelectRuleMapper extends BaseMapper<SelectRule> {
}
