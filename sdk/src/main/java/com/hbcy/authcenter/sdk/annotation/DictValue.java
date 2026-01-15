package com.hbcy.authcenter.sdk.annotation;

import com.hbcy.authcenter.sdk.bean.DictValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 判断字典项是否合法
 *
 * @author 姚泰然
 * @date 2026-01-15 16:24
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DictValueValidator.class)
public @interface DictValue {
    String message() default "参数错误，枚举值不在允许范围内";

    /**
     * 字典类型编码
     */
    String featCode() default "";

    /**
     * 父节点id，为空表示不校验父节点
     */
    String parentId() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
