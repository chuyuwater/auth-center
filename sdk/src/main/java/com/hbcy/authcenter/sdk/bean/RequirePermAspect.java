package com.hbcy.authcenter.sdk.bean;

import com.hbcy.authcenter.sdk.annotation.RequirePerm;
import com.hbcy.authcenter.sdk.feign.AuthCenterClient;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.pojo.ApiResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 权限校验切面
 *
 * @author 姚泰然
 * @date 2026-03-06
 */
@Aspect
@Slf4j
public class RequirePermAspect {

    @Resource
    private AuthCenterClient authCenterClient;

    @Around("@annotation(com.hbcy.authcenter.sdk.annotation.RequirePerm)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequirePerm requirePerm = AnnotatedElementUtils.findMergedAnnotation(method, RequirePerm.class);
        if (requirePerm == null) {
            return joinPoint.proceed();
        }

        String code = requirePerm.code();
        if (!StringUtils.hasText(code)) {
            code = requirePerm.value();
        }

        if (StringUtils.hasText(code)) {
            ApiResponse<Boolean> response = authCenterClient.checkPerm(code);
            if (response == null || !Integer.valueOf(0).equals(response.getStatus())
                    || !Boolean.TRUE.equals(response.getData())) {
                log.warn("check perm fail, code: {}, response: {}", code, response);
                throw new PermissionError();
            }
        }

        return joinPoint.proceed();
    }
}
