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
 * 安全公共配置类。
 *
 * <p>此配置类位于 clx-common-security 模块中，被所有需要安全认证的服务共享。
 * 目前提供以下配置：
 *
 * <ul>
 *   <li>BCryptPasswordEncoder：密码加密器，用于用户密码的加密和验证</li>
 *   <li>CorsFilter：跨域过滤器，处理前端跨域请求</li>
 * </ul>
 *
 * <p>BCrypt 说明：
 * <ul>
 *   <li>BCrypt是一种自适应哈希算法，专门用于密码存储</li>
 *   <li>每次加密会生成不同的哈希值（内置随机盐）</li>
 *   <li>计算成本可调（默认10轮），可抵抗暴力破解</li>
 *   <li>输出固定60字符长度</li>
 * </ul>
 *
 * <p>CORS 说明：
 * <ul>
 *   <li>允许指定的前端域名访问后端API</li>
 *   <li>支持常用HTTP方法（GET、POST、PUT、DELETE、OPTIONS）</li>
 *   <li>允许携带Authorization头</li>
 * </ul>
 */
@Configuration
public class SaTokenConfig {

    /**
     * 创建 BCrypt 密码加密器 Bean。
     *
     * <p>此Bean是单例的，整个应用共享一个实例。
     * BCryptPasswordEncoder 是线程安全的。
     *
     * <p>使用方式：
     * <pre>
     * // 加密密码
     * String encoded = passwordEncoder.encode("rawPassword");
     *
     * // 验证密码
     * boolean matches = passwordEncoder.matches("rawPassword", encoded);
     * </pre>
     *
     * @return BCrypt密码加密器实例
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 创建 CORS 跨域过滤器 Bean。
     *
     * <p>配置允许的前端域名、HTTP方法、请求头等。
     *
     * <p>配置来源：
     * <ul>
     *   <li>cors.allowed-origins：允许的前端域名（逗号分隔）</li>
     *   <li>cors.allow-credentials：是否允许携带Cookie</li>
     * </ul>
     *
     * <p>注意：如果 allow-credentials=true，则不能使用 "*" 作为 allowedOrigins，
     * 需要明确指定域名列表。
     *
     * @param allowedOrigins 允许的前端域名（逗号分隔）
     * @param allowCredentials 是否允许携带Cookie
     * @return CORS过滤器实例
     */
    @Bean
    public CorsFilter corsFilter(
            @Value("${cors.allowed-origins:http://localhost:5173}") String allowedOrigins,
            @Value("${cors.allow-credentials:false}") boolean allowCredentials
    ) {
        CorsConfiguration config = new CorsConfiguration();

        // 设置允许的域名（支持通配符模式）
        config.setAllowedOriginPatterns(splitCsv(allowedOrigins));

        // 设置允许的HTTP方法
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 设置允许的请求头（* 表示所有）
        config.setAllowedHeaders(List.of("*"));

        // 设置是否允许携带Cookie
        config.setAllowCredentials(allowCredentials);

        // 设置暴露给前端的响应头（前端需要读取Token）
        config.setExposedHeaders(List.of("Authorization"));

        // 设置预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        // 注册配置到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

    /**
     * 分割逗号分隔的字符串为列表。
     *
     * <p>用于解析配置文件中的域名列表，如：
     * "http://localhost:5173,http://localhost:3000"
     *
     * @param value 逗号分隔的字符串
     * @return 分割后的列表（已去除空白和空项）
     */
    private List<String> splitCsv(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}