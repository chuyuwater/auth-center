package com.hbcy.authcenter.api.modules.sys.tools.service;

import cn.hutool.core.io.file.FileNameUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.ServerError;
import com.hbcy.common.base.uploader.UploadResultDTO;
import com.pig4cloud.plugin.oss.OssProperties;
import com.pig4cloud.plugin.oss.service.OssTemplate;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author 姚泰然
 * @date 2025-12-23 10:40
 */
@Component
public class UploadService {
    @Resource
    private OssTemplate ossTemplate;
    @Resource
    private OssProperties ossProperties;

    /**
     * 小文件上报
     */
    public UploadResultDTO upload(MultipartFile file) {
        String[] allowSuffix = {"jpg", "jpeg", "png", "pdf", "doc", "md", "docx", "ppt", "pptx", "xls", "xlsx", "sql"};
        try {
            String name = file.getOriginalFilename();
            String suffix = FileNameUtil.getSuffix(name);
            if (!Strings.CS.endsWithAny(suffix, allowSuffix)) {
                throw new ParamError("后缀不在允许范围内");
            }
            String uploadName = UlidCreator.getUlid().toString() + "." + suffix;
            // 使用 BufferedInputStream 包装
            try (InputStream is = new BufferedInputStream(file.getInputStream())) {
                ossTemplate.putObject(ossProperties.getBucketName(), uploadName, is);
            }
            UploadResultDTO dto = new UploadResultDTO();
            dto.setFileName(uploadName);
            dto.setUrl(ossProperties.getEndpoint() + "/" + ossProperties.getBucketName() + "/" + uploadName);
            if (StringUtils.isNotBlank(ossProperties.getCustomDomain())) {
                dto.setExternalUrl(ossProperties.getCustomDomain() + "/" + ossProperties.getBucketName() + "/" + uploadName);
            }
            return dto;
        } catch (IOException e) {
            throw new ServerError("fail to upload");
        }
    }
}
