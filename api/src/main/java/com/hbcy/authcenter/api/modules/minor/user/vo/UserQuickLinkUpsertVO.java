package com.hbcy.authcenter.api.modules.minor.user.vo;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-02-25 16:53
 */
@Data
public class UserQuickLinkUpsertVO {
    /**
     * 菜单id，有序
     */
    @Size(max = 9, message = "快捷入口数量不能超过9个")
    private List<String> resIds;
    /**
     * 客户端类型, 1-PC, 2-移动端
     */
    @JsonSetter(nulls = Nulls.SKIP)
    @Range(min = 1, max = 2, message = "客户端类型错误")
    private Integer clientType = 0;
}
