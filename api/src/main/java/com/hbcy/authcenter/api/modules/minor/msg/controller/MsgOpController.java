package com.hbcy.authcenter.api.modules.minor.msg.controller;

import com.hbcy.authcenter.api.modules.minor.msg.dto.MsgDTO;
import com.hbcy.authcenter.api.modules.minor.msg.dto.SourceAppDTO;
import com.hbcy.authcenter.api.modules.minor.msg.service.UserMsgService;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgQueryVO;
import com.hbcy.common.base.pojo.PageResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * 查询消息（分页，按接收时间倒序）
     *
     * @param vo 查询条件
     * @return 消息分页结果
     */
    @GetMapping
    public PageResp<MsgDTO> queryMsg(UserMsgQueryVO vo) {
        return userMsgService.listMsg(vo);
    }

    /**
     * 查询已推送消息的应用列表（用于消息来源下拉）
     *
     * @return 应用 id 与名称列表
     */
    @GetMapping("/source-apps")
    public List<SourceAppDTO> listSourceApps() {
        return userMsgService.listMsgSourceApps();
    }

    /**
     * 根据 id 查询消息详情
     *
     * @param id 消息 id
     * @return 消息详情，无权限或不存在时返回 null
     */
    @GetMapping("/{id}")
    public MsgDTO getMsgById(@PathVariable String id) {
        return userMsgService.getMsgById(id);
    }
}
