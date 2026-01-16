package com.hbcy.authcenter.api.modules.sys.dict.vo;

import com.hbcy.common.db.model.PageVO;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典查询VO
 *
 * @author 姚泰然
 * @date 2025-12-22 14:48
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DictQueryVO extends PageVO {
    /**
     * 字典类型编码
     */
    @NotBlank(message = "字典类型编码不能为空")
    private String featCode;
    /**
     * 关键字
     */
    private String keyword;
}
