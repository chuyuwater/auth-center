package com.hbcy.authcenter.api.modules.minor.inbox.vo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-27 09:00
 */
@Data
public class UserMsgBatchOpVO {
    @NotEmpty(message = "msgIds不能为空")
    private List<String> msgIds;
}
