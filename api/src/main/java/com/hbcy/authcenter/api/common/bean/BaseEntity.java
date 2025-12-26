package com.hbcy.authcenter.api.common.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * model层继承该类，方便返回时注入用户名
 *
 * @author 姚泰然
 * @date 2025-12-26 17:48
 */
@Data
public class BaseEntity {
    //待注入
    @TableField(exist = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createUserName;
    @TableField(exist = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String updateUserName;
}
