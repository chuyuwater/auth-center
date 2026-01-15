package com.hbcy.authcenter.sdk.bean;

import com.hbcy.authcenter.sdk.annotation.DictValue;
import com.hbcy.authcenter.sdk.feign.AuthCenterClient;
import com.hbcy.authcenter.sdk.feign.dto.SysDictDTO;
import com.hbcy.common.base.error.ServerError;
import com.hbcy.common.base.pojo.ApiResponse;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-15 16:27
 */
@Component
@Slf4j
public class DictValueValidator implements ConstraintValidator<DictValue, Object> {
    private String featCode;
    private String parentId;

    @Resource
    private AuthCenterClient authCenterClient;

    @Override
    public void initialize(DictValue constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        featCode = constraintAnnotation.featCode();
        parentId = constraintAnnotation.parentId();
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        try {
            ApiResponse<List<SysDictDTO>> response = authCenterClient.listDictByFeatCode(featCode, parentId);
            if (!response.getStatus().equals(0)) {
                log.error("fail to query dict value:{}", response.getMsg());
                return false;
            }
            if (CollectionUtils.isEmpty(response.getData())) {
                return false;
            }
            return response.getData().stream().anyMatch(
                    dict -> dict.getValueStr().equals(o.toString()));
        } catch (Exception e) {
            throw new ServerError("服务内部通信错误，请联系管理员");
        }
    }
}
