package com.clx.common.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * sa-Token 配置类
 *
 * 包含 CORS 配置和密码加密器
 */
@Configuration
public class SaTokenConfig {

    /**
     * BCrypt 密码加密器
     * 单例 Bean，避免每次登录都创建新实例
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 过滤器配置
     * 允许前端跨域访问后端接口
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的前端地址（生产环境通过环境变量配置）
        String allowedOrigins = System.getProperty("CORS_ALLOWED_ORIGINS", "http://localhost:5173");
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        // 允许的方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的请求头
        config.setAllowedHeaders(List.of("*"));
        // 允许携带凭证（Cookie、Authorization header）
        config.setAllowCredentials(true);
        // 暴露的响应头（前端可以读取）
        config.setExposedHeaders(List.of("Authorization"));
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}