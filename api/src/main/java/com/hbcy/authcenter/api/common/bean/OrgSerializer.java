package com.hbcy.authcenter.api.common.bean;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hbcy.authcenter.api.modules.core.org.service.OrgTreeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author 姚泰然
 * @date 2025-12-26 14:14
 */
@Component
public class OrgSerializer extends JsonSerializer<String> {
    @Resource
    private OrgTreeService orgTreeService;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

    }
}
