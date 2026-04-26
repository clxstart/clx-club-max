package com.clx.post.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 配置。
 */
@Configuration
public class HttpConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}