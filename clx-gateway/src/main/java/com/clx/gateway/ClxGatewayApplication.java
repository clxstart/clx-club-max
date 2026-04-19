package com.clx.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API网关启动类
 *
 * 职责：
 * 1. 统一入口，路由转发
 * 2. 认证过滤（Token验证）
 * 3. 限流防护
 * 4. CORS/XSS安全配置
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ClxGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClxGatewayApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  API网关启动成功   ლ(´ڡ`ლ)ﾞ");
    }

}