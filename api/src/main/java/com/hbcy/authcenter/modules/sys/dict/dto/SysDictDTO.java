package com.hbcy.authcenter.modules.sys.dict.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 姚泰然
 * @date 2025-12-22 13:45
 */
@Data
@NoArgsConstructor
public class SysDictDTO {
    private String id;
    private String featCode;
    private String valueStr;
    private String valueCn;
    private String parentId;
    private Integer showOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
