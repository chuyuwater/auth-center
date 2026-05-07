package com.hbcy.authcenter.api.modules.minor.msg.controller;

import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgChannelDTO;
import com.hbcy.authcenter.api.modules.minor.msg.service.MsgChannelService;
import com.hbcy.authcenter.api.modules.minor.msg.vo.ChannelCreateVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.ChannelQueryVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.ChannelUpdateVO;
import com.hbcy.common.base.pojo.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 消息渠道管理
 */
@RestController
@RequestMapping("/api/portal/v1/msg/channel")
public class MsgChannelController {
    @Resource
    private MsgChannelService msgChannelService;

    @GetMapping
    public PageResp<MsgChannelDTO> queryChannel(ChannelQueryVO vo) {
        return msgChannelService.queryChannel(vo);
    }

    @GetMapping("/{id}")
    public MsgChannelDTO getChannelDetail(@PathVariable String id) {
        return msgChannelService.getChannelDetail(id);
    }

    @PostMapping
    public MsgChannelDTO createChannel(@Valid @RequestBody ChannelCreateVO vo) {
        return msgChannelService.createChannel(vo);
    }

    @PutMapping("/{id}")
    public MsgChannelDTO updateChannel(@PathVariable String id, @Valid @RequestBody ChannelUpdateVO vo) {
        return msgChannelService.updateChannel(id, vo);
    }

    @PutMapping("/{id}/toggle")
    public void toggleChannel(@PathVariable String id) {
        msgChannelService.toggleChannel(id);
    }

    @DeleteMapping("/{id}")
    public void deleteChannel(@PathVariable String id) {
        msgChannelService.deleteChannel(id);
    }
}
