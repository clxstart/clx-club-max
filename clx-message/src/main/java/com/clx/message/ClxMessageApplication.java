package com.clx.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 消息服务启动类
 *
 * 职责：
 * 1. 私信实时投递（WebSocket）
 * 2. 通知系统（评论回复/点赞/关注/系统公告）
 * 3. 在线状态管理
 */
@SpringBootApplication(scanBasePackages = {"com.clx.common.redis", "com.clx.message"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.clx")
@MapperScan("com.clx.message.mapper")
@EnableScheduling
public class ClxMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClxMessageApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  消息服务启动成功   ლ(´ڡ`ლ)ﾞ");
    }

}
