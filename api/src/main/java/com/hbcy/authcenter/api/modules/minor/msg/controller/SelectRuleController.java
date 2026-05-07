package com.hbcy.authcenter.api.modules.minor.msg.controller;

import com.hbcy.authcenter.api.modules.minor.msg.dto.SelectRuleDTO;
import com.hbcy.authcenter.api.modules.minor.msg.model.SelectRuleCondition;
import com.hbcy.authcenter.api.modules.minor.msg.service.SelectRuleService;
import com.hbcy.authcenter.api.modules.minor.msg.vo.RuleCreateVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.RuleQueryVO;
import com.hbcy.common.base.pojo.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portal/v1/msg/rule")
public class SelectRuleController {
    @Resource
    private SelectRuleService selectRuleService;

    @GetMapping
    public PageResp<SelectRuleDTO> queryRule(RuleQueryVO vo) {
        return selectRuleService.queryRule(vo);
    }

    @GetMapping("/{id}")
    public SelectRuleDTO getRuleDetail(@PathVariable String id) {
        return selectRuleService.getRuleDetail(id);
    }

    @GetMapping("/{id}/conditions")
    public List<SelectRuleCondition> listConditions(@PathVariable String id) {
        return selectRuleService.listConditions(id);
    }

    @PostMapping
    public SelectRuleDTO createRule(@Valid @RequestBody RuleCreateVO vo) {
        return selectRuleService.createRule(vo);
    }

    @PutMapping("/{id}")
    public SelectRuleDTO updateRule(@PathVariable String id, @Valid @RequestBody RuleCreateVO vo) {
        return selectRuleService.updateRule(id, vo);
    }

    @PutMapping("/{id}/toggle")
    public void toggleRule(@PathVariable String id) {
        selectRuleService.toggleRule(id);
    }

    @DeleteMapping("/{id}")
    public void deleteRule(@PathVariable String id) {
        selectRuleService.deleteRule(id);
    }
}
