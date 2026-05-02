package com.clx.analytics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据分析服务启动类
 * 端口：9800
 * 功能：行为日志采集、报表查询
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@MapperScan("com.clx.analytics.mapper")
public class ClxAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClxAnalyticsApplication.class, args);
    }
}
