package com.clx.post;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 帖子服务启动类。
 *
 * <p>启动后访问：
 * <ul>
 *   <li>帖子列表：GET http://localhost:9300/post/list</li>
 *   <li>API文档：http://localhost:9300/swagger-ui.html</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.clx")
@MapperScan("com.clx.post.mapper")
@EnableScheduling
public class ClxPostApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClxPostApplication.class, args);
        System.out.println("""

                ██████╗██╗     ██╗   ██╗██████╗ ███████╗██████╗
               ██╔════╝██║     ██║   ██║██╔══██╗██╔════╝██╔══██╗
               ██║     ██║     ██║   ██║██║  ██║█████╗  ██████╔╝
               ██║     ██║     ██║   ██║██║  ██║██╔══╝  ██╔══██╗
               ╚██████╗███████╗╚██████╔╝██████╔╝███████╗██║  ██║
                ╚═════╝╚══════╝ ╚═════╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝

                帖子服务启动成功，端口: 9300
                """);
    }
}