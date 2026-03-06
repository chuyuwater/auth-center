package com.hbcy.authcenter.sdk.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 只能在方法上使用，作用是调用AuthCenterClient的checkPerm接口，如果返回false，则抛出PermissionError
 *
 * @author 姚泰然
 * @date 2026-03-06
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePerm {
    /**
     * 权限编码
     */
    @AliasFor("code")
    String value() default "";

    /**
     * 权限编码，与value等价
     */
    @AliasFor("value")
    String code() default "";
}
