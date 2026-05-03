package com.clx.common.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * 安全公共配置。
 *
 * <p>提供密码加密器和跨域过滤器。
 */
@Configuration
public class SaTokenConfig {

    /**
     * BCrypt 密码加密器。
     *
     * <p>加密：passwordEncoder.encode("密码")
     * <p>验证：passwordEncoder.matches("原密码", "加密后")
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 跨域过滤器。
     *
     * @param allowedOrigins   允许的前端域名（逗号分隔）
     * @param allowCredentials 是否允许携带 Cookie
     */
    @Bean
    public CorsFilter corsFilter(
            @Value("${cors.allowed-origins:http://localhost:5173}") String allowedOrigins,
            @Value("${cors.allow-credentials:false}") boolean allowCredentials
    ) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(splitCsv(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(allowCredentials);
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /** 分割逗号分隔的字符串 */
    private List<String> splitCsv(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
