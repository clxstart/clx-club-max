package com.clx.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 后台管理服务启动类。
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.clx.admin.feign")
@MapperScan("com.clx.admin.mapper")
public class ClxAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClxAdminApplication.class, args);
    }
}
