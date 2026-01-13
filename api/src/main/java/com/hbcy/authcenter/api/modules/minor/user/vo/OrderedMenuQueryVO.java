package com.hbcy.authcenter.api.modules.minor.user.vo;

import lombok.Data;

import java.util.Collection;

/**
 * @author 姚泰然
 * @date 2026-01-13 09:28
 */
@Data
public class OrderedMenuQueryVO {
    private Collection<String> resIds;
    private Collection<Integer> showLevels;
    private Collection<Integer> clientTypes;
}
