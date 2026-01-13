package com.hbcy.authcenter.api.common.enums;

import com.hbcy.common.db.dictvalue.BaseDictValue;

/**
 * @author 姚泰然
 * @date 2026-01-12 16:40
 */
public class ClientTypeEnum extends BaseDictValue<Integer> {
    public static final int ALL = 0;
    public static final int PC = 1;
    public static final int MOBILE = 2;

    @Override
    public String getDictCode() {
        return "CLIENT_TYPE";
    }
}
