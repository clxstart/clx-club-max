package com.clx.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 认证中心启动类。
 *
 * <p>启动后访问：
 * <ul>
 *   <li>登录接口：POST http://localhost:9100/auth/login</li>
 *   <li>API文档：http://localhost:9100/swagger-ui.html</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.clx")
@MapperScan("com.clx.auth.mapper")
public class ClxAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClxAuthApplication.class, args);
        System.out.println("""

                ██████╗██╗     ██╗   ██╗██████╗ ███████╗██████╗
               ██╔════╝██║     ██║   ██║██╔══██╗██╔════╝██╔══██╗
               ██║     ██║     ██║   ██║██║  ██║█████╗  ██████╔╝
               ██║     ██║     ██║   ██║██║  ██║██╔══╝  ██╔══██╗
               ╚██████╗███████╗╚██████╔╝██████╔╝███████╗██║  ██║
                ╚═════╝╚══════╝ ╚═════╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝

                认证中心启动成功，端口: 9100
                """);
    }
}