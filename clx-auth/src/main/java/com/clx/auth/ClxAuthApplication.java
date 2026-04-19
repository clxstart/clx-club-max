package com.clx.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * 认证中心启动类
 */
@SpringBootApplication(
    scanBasePackages = "com.clx",
    exclude = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    }
)
@MapperScan("com.clx.auth.mapper")
public class ClxAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClxAuthApplication.class, args);
        System.out.println("""

              ██████╗██╗      ██████╗ ██╗   ██╗██████╗  ██████╗
             ██╔════╝██║     ██╔═══██╗██║   ██║██╔══██╗██╔═══██╗
             ██║     ██║     ██║   ██║██║   ██║██║  ██║██║   ██║
             ██║     ██║     ██║   ██║██║   ██║██║  ██║██║   ██║
             ╚██████╗███████╗╚██████╔╝╚██████╔╝██████╔╝╚██████╔╝
              ╚═════╝╚══════╝ ╚═════╝  ╚═════╝ ╚═════╝  ╚═════╝

                    认证中心启动成功 ✓ 端口: 9100
            """);
    }
}
