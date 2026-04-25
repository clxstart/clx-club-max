package com.clx.quiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 刷题服务启动类
 * 端口：9600
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ClxQuizApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClxQuizApplication.class, args);
    }
}