package com.hbcy.authcenter.api.modules.sys.tools.service;

import cn.hutool.core.io.file.FileNameUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.uploader.UploadResultDTO;
import com.hbcy.common.oss.OssTemplate;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author 姚泰然
 * @date 2025-12-23 10:40
 */
@Component
@Slf4j
public class UploadService {
    @Resource
    private OssTemplate ossTemplate;

    /**
     * 小文件上报
     */
    public UploadResultDTO upload(MultipartFile file) {
        String[] allowSuffix = {"jpg", "jpeg", "png", "pdf", "doc", "md", "docx", "ppt", "pptx", "xls", "xlsx", "sql"};
        String name = file.getOriginalFilename();
        String suffix = FileNameUtil.getSuffix(name);
        if (!Strings.CS.endsWithAny(suffix, allowSuffix)) {
            throw new ParamError("后缀不在允许范围内");
        }
        String contentType = MediaTypeFactory.getMediaType(file.getOriginalFilename())
                .map(MediaType::toString)
                .orElse("application/octet-stream");
        String uploadName = UlidCreator.getUlid().toString() + "." + suffix;
        return ossTemplate.putObject(uploadName, RequestBody.fromContentProvider(
                () -> {
                    try {
                        return file.getInputStream();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                },
                file.getSize(),
                contentType
        ));
    }
}
