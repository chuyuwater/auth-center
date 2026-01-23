package com.hbcy.authcenter.api.common.enums;

import com.hbcy.common.db.dictvalue.BaseDictValue;

/**
 * @author 姚泰然
 * @date 2026-01-23 10:24
 */
public class EmployeeTypeEnum extends BaseDictValue<Integer> {
    public static final int EXTERNAL = 4;

    @Override
    public String getDictCode() {
        return "EMPLOYEE_TYPE";
    }
}
