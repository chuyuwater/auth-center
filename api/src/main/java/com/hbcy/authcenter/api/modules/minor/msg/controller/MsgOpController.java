package com.hbcy.authcenter.api.modules.minor.msg.controller;

import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgDTO;
import com.hbcy.authcenter.api.modules.minor.msg.service.UserMsgService;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgQueryVO;
import com.hbcy.common.base.pojo.PageResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息管理
 * @author 姚泰然
 * @module msg
 * @date 2026-03-11 08:31
 */
@RestController
@RequestMapping("/api/portal/v1/msg")
public class MsgOpController {
    @Resource
    private UserMsgService userMsgService;

    /**
     * 查询消息
     *
     * @param vo 查询条件
     * @return 消息分页结果
     */
    @GetMapping
    public PageResp<MsgDTO> queryMsg(UserMsgQueryVO vo) {
        return userMsgService.listMsg(vo);
    }
}
