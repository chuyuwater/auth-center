package com.hbcy.authcenter.sdk.feign.dto;

import lombok.Data;

/**
 * @author 姚泰然
 * @date 2026-01-15 16:20
 */
@Data
public class SysDictDTO {
    /**
     * 字典项id
     */
    private String id;
    /**
     * 字典类型编码
     */
    private String featCode;
    /**
     * 字典项编码
     */
    private String valueStr;
    /**
     * 字典像名称
     */
    private String valueCn;
}
