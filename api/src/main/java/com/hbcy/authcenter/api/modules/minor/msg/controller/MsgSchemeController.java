package com.hbcy.authcenter.api.modules.minor.msg.controller;

import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgSchemeDTO;
import com.hbcy.authcenter.api.modules.minor.msg.model.MsgSchemeChannel;
import com.hbcy.authcenter.api.modules.minor.msg.service.MsgSchemeService;
import com.hbcy.authcenter.api.modules.minor.msg.vo.SchemeCreateVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.SchemeQueryVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.SchemeUpdateVO;
import com.hbcy.common.base.pojo.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portal/v1/msg/scheme")
public class MsgSchemeController {
    @Resource
    private MsgSchemeService msgSchemeService;

    @GetMapping
    public PageResp<MsgSchemeDTO> queryScheme(SchemeQueryVO vo) {
        return msgSchemeService.queryScheme(vo);
    }

    @GetMapping("/{id}")
    public MsgSchemeDTO getSchemeDetail(@PathVariable String id) {
        return msgSchemeService.getSchemeDetail(id);
    }

    @GetMapping("/{id}/channels")
    public List<MsgSchemeChannel> listSchemeChannels(@PathVariable String id) {
        return msgSchemeService.listSchemeChannels(id);
    }

    @PostMapping
    public MsgSchemeDTO createScheme(@Valid @RequestBody SchemeCreateVO vo) {
        return msgSchemeService.createScheme(vo);
    }

    @PutMapping("/{id}")
    public MsgSchemeDTO updateScheme(@PathVariable String id, @Valid @RequestBody SchemeUpdateVO vo) {
        return msgSchemeService.updateScheme(id, vo);
    }

    @PutMapping("/{id}/toggle")
    public void toggleScheme(@PathVariable String id) {
        msgSchemeService.toggleScheme(id);
    }

    @DeleteMapping("/{id}")
    public void deleteScheme(@PathVariable String id) {
        msgSchemeService.deleteScheme(id);
    }
}
