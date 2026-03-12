package com.hbcy.authcenter.api.config;

import com.hbcy.authcenter.api.common.enums.TodoQueryScopeEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

/**
 * 请求参数与枚举等类型的转换配置
 *
 * @author 姚泰然
 * @date 2026-03-12
 */
@Configuration
public class WebConvertConfig {

    @Bean
    public Converter<String, TodoQueryScopeEnum> todoQueryScopeConverter() {
        return TodoQueryScopeEnum::fromCode;
    }
}
