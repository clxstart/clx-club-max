package com.clx.message.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 安全配置。
 *
 * 当前阶段：不启用 Spring Security，认证由 sa-Token 在业务层处理。
 * WebSocket 握手认证在 WsHandshakeInterceptor 中完成。
 * CORS 配置在 CorsConfig 中处理。
 */
@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    // sa-Token 配置由 clx-common-security 提供
    // 无需额外 Spring Security 配置
}
