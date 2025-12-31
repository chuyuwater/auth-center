package com.hbcy.authcenter.api.modules.sys.tools.controller;

import com.hbcy.authcenter.api.modules.sys.tools.service.UploadService;
import com.hbcy.common.base.uploader.UploadResultDTO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 工具接口
 *
 * @author 姚泰然
 * @module sys
 * @date 2025-12-23 10:23
 */
@RestController
@RequestMapping("api/portal/v1/sys/tools")
public class Uploader {

    @Resource
    private UploadService uploadService;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    public UploadResultDTO upload(@RequestParam("file") MultipartFile file) {
        return uploadService.upload(file);
    }
}

