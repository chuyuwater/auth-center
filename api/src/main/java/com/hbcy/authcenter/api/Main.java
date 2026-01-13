package com.hbcy.authcenter.api;

import com.hbcy.common.base.log.JsonLogUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.hbcy")
@EnableDiscoveryClient
@MapperScan(basePackages = "com.hbcy.authcenter.api.**.dao")
@EnableFeignClients(basePackages = "com.hbcy")
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        JsonLogUtils.log("service start", "app", "portal");
    }
}