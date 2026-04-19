package com.clx.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动类
 *
 * 职责：
 * 1. 用户信息管理（CRUD）
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.clx")
@MapperScan("com.clx.user.mapper")
public class ClxUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClxUserApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  用户服务启动成功   ლ(´ڡ`ლ)ﾞ");
    }

}