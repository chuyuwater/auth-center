package com.hbcy.authcenter.api.modules.sys.tools.service;

import cn.hutool.core.io.file.FileNameUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.ServerError;
import com.hbcy.common.base.uploader.UploadResultDTO;
import com.pig4cloud.plugin.oss.OssProperties;
import com.pig4cloud.plugin.oss.service.OssTemplate;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author 姚泰然
 * @date 2025-12-23 10:40
 */
@Component
@Slf4j
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
            //这里只适用于上传小文件，大文件应直接上传到oss，而不是通过服务端二次传递
            //FIXME：解决文件内存二次复制的问题，理论上不需要，但是pig4cloud的实现有问题
            ossTemplate.putObject(ossProperties.getBucketName(), uploadName, new ByteArrayInputStream(file.getBytes()));
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
