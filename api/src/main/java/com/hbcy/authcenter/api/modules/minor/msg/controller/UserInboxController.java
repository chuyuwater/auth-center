package com.hbcy.authcenter.api.modules.minor.msg.controller;

import com.hbcy.authcenter.api.modules.minor.msg.dto.UserInboxDTO;
import com.hbcy.authcenter.api.modules.minor.msg.service.UserInboxService;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgBatchOpVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgCreateVO;
import com.hbcy.authcenter.api.modules.minor.msg.vo.UserMsgQueryVO;
import com.hbcy.common.base.pojo.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户消息
 *
 * @author 姚泰然
 * @module msg
 * @date 2026-01-27 09:52
 */
@RestController
@RequestMapping("api/portal/v1/user/inbox")
public class UserInboxController {
    @Resource
    private UserInboxService userInboxService;


    /**
     * 查询消息列表
     *
     * @param vo 查询条件
     * @return 消息分页结果
     */
    @GetMapping
    public PageResp<UserInboxDTO> queryMsg(UserMsgQueryVO vo) {
        return userInboxService.queryMsg(vo);
    }

    /**
     * 批量创建消息
     * 供第三方系统调用
     *
     * @param vo 消息创建参数
     */
    @PostMapping
    public void batchCreateMsg(@Valid @RequestBody UserMsgCreateVO vo) {
        userInboxService.batchCreateMsg(vo);
    }

    /**
     * 标记消息为已读
     *
     * @param vo 操作参数
     */
    @PostMapping("/mark-as-read")
    public void markAsRead(@Valid @RequestBody UserMsgBatchOpVO vo) {
        userInboxService.markAsRead(vo);
    }

    /**
     * 批量删除消息
     *
     * @param vo 操作参数
     */
    @PostMapping("/batch-delete")
    public void batchDelete(@Valid @RequestBody UserMsgBatchOpVO vo) {
        userInboxService.batchDelete(vo);
    }
}
