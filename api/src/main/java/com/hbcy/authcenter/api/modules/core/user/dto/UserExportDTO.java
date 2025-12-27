package com.hbcy.authcenter.api.modules.core.user.dto;

import cn.idev.excel.annotation.ExcelProperty;
import com.hbcy.authcenter.api.modules.core.user.vo.UserImportVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 姚泰然
 * @date 2025-12-27 13:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserExportDTO extends UserImportVO {
    @ExcelProperty(value = "用户ID", order = 0)
    private String id;
}
