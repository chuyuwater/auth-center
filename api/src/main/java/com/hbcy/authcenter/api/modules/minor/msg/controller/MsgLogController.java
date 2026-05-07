package com.hbcy.authcenter.api.modules.minor.msg.controller;

import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgLogDTO;
import com.hbcy.authcenter.api.modules.minor.msg.service.MsgLogService;
import com.hbcy.authcenter.api.modules.minor.msg.vo.MsgLogQueryVO;
import com.hbcy.common.base.pojo.PageResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portal/v1/msg/log")
public class MsgLogController {
    @Resource
    private MsgLogService msgLogService;

    @GetMapping
    public PageResp<MsgLogDTO> queryMsgLog(MsgLogQueryVO vo) {
        return msgLogService.queryMsgLog(vo);
    }

    @GetMapping("/{id}")
    public MsgLogDTO getMsgLogDetail(@PathVariable String id) {
        return msgLogService.getMsgLogDetail(id);
    }

    @PostMapping("/{id}/retry")
    public void retryMsg(@PathVariable String id) {
        // TODO: 接入MsgSendEngine实现手动重发
    }
}
