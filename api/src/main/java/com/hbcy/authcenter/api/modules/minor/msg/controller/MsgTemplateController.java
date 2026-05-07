package com.hbcy.authcenter.api.modules.minor.msg.controller;

import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgTemplateContentDTO;
import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgTemplateDTO;
import com.hbcy.authcenter.api.modules.minor.msg.service.MsgTemplateService;
import com.hbcy.authcenter.api.modules.minor.msg.vo.*;
import com.hbcy.common.base.pojo.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息模板管理
 */
@RestController
@RequestMapping("/api/portal/v1/msg/template")
public class MsgTemplateController {
    @Resource
    private MsgTemplateService msgTemplateService;

    @GetMapping
    public PageResp<MsgTemplateDTO> queryTemplate(TemplateQueryVO vo) {
        return msgTemplateService.queryTemplate(vo);
    }

    @GetMapping("/{id}")
    public MsgTemplateDTO getTemplateDetail(@PathVariable String id) {
        return msgTemplateService.getTemplateDetail(id);
    }

    @GetMapping("/{id}/contents")
    public List<MsgTemplateContentDTO> listContents(@PathVariable String id) {
        return msgTemplateService.listContents(id);
    }

    @PostMapping
    public MsgTemplateDTO createTemplate(@Valid @RequestBody TemplateCreateVO vo) {
        return msgTemplateService.createTemplate(vo);
    }

    @PutMapping("/{id}")
    public MsgTemplateDTO updateTemplate(@PathVariable String id, @Valid @RequestBody TemplateUpdateVO vo) {
        return msgTemplateService.updateTemplate(id, vo);
    }

    @PutMapping("/{id}/toggle")
    public void toggleTemplate(@PathVariable String id) {
        msgTemplateService.toggleTemplate(id);
    }

    @DeleteMapping("/{id}")
    public void deleteTemplate(@PathVariable String id) {
        msgTemplateService.deleteTemplate(id);
    }

    @PostMapping("/{id}/content")
    public MsgTemplateContentDTO addContent(@PathVariable String id, @Valid @RequestBody TemplateContentVO vo) {
        return msgTemplateService.addContent(id, vo);
    }

    @PutMapping("/content/{contentId}")
    public MsgTemplateContentDTO updateContent(@PathVariable String contentId, @Valid @RequestBody TemplateContentVO vo) {
        return msgTemplateService.updateContent(contentId, vo);
    }

    @PostMapping("/content/{contentId}/copy")
    public MsgTemplateContentDTO copyContent(@PathVariable String contentId) {
        return msgTemplateService.copyContent(contentId);
    }

    @DeleteMapping("/content/{contentId}")
    public void deleteContent(@PathVariable String contentId) {
        msgTemplateService.deleteContent(contentId);
    }
}
