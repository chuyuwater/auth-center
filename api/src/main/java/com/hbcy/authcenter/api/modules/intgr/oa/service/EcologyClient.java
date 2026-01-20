package com.hbcy.authcenter.api.modules.intgr.oa.service;

import com.hbcy.authcenter.api.modules.intgr.oa.dto.OaRegisterRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 泛微E9接口（OA）
 * @author 姚泰然
 * @date 2026-01-19 13:51
 */
@FeignClient(name = "oa", url = "${oa.url:http://www.chuyuwater.cn:8088}")
public interface EcologyClient {
    /** 注册接口 */
    @PostMapping(value = "/api/ec/dev/auth/regist", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    OaRegisterRespDTO regist(@RequestHeader("appid") String appid, @RequestHeader("cpk") String cpk);

    /** 获取 Token */
    @PostMapping(value = "/api/ec/dev/auth/applytoken", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String applyToken(@RequestHeader("appid") String appid,
                      @RequestHeader("secret") String encryptedSecret,
                      @RequestHeader("time") String time);


    @PostMapping(value = "/api/doc/upload/uploadFile2Doc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestHeader("appid") String appid,
                      @RequestHeader("token") String token,
                      @RequestHeader("userid") String encryptedUserid,
                      @RequestPart("file") MultipartFile file,
                      @RequestParam Map<String, Object> params);
}
