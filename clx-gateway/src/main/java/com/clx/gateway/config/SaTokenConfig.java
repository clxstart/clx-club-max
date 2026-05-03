package com.clx.gateway.config;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * sa-Token JWT 整合配置（Reactive 版本）。
 *
 * <p>使用 Simple 模式：Token 格式为 JWT，会话数据存储在 Redis 中。
 * 这样可以保留踢人下线、权限刷新等完整功能。
 */
@Configuration
public class SaTokenConfig {

    /**
     * 注册 Sa-Token JWT Simple 模式的 StpLogic。
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
}
