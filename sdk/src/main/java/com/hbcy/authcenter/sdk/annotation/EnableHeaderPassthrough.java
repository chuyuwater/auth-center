package com.hbcy.authcenter.sdk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 姚泰然
 * @date 2025-12-23 13:15
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableHeaderPassthrough {
    // 可以扩展：指定透传哪些具体的 header，默认全部透传
    String[] value() default {};
}
