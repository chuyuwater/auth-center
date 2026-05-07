package com.hbcy.authcenter.api.modules.minor.msg.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbcy.authcenter.api.modules.minor.msg.model.SelectRuleCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 选人规则条件 Mapper
 */
@Mapper
public interface SelectRuleConditionMapper extends BaseMapper<SelectRuleCondition> {

    @Select("SELECT * FROM select_rule_condition WHERE rule_id = #{ruleId} ORDER BY show_order")
    List<SelectRuleCondition> selectByRuleId(@Param("ruleId") String ruleId);
}
