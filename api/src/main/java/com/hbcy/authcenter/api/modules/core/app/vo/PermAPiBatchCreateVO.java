package com.hbcy.authcenter.api.modules.core.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-03-10 11:32
 */
@Data
@AllArgsConstructor
public class PermAPiBatchCreateVO {
    private String permId;
    private List<ResourcePermApiVO> apis;
}
