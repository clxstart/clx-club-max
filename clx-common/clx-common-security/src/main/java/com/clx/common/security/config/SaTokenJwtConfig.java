package com.clx.common.security.config;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * sa-Token JWT 配置。
 *
 * <p>使用
 * <p>优点：支持踢人下线、权限实时刷新。
 */
@Configuration
public class SaTokenJwtConfig {

    /**
     * 注册 JWT Simple 模式。
     *
     * <p>Token 格式从 UUID 切换为 JWT（三段式字符串）。
     *
     * @return JWT Simple 模式的 StpLogic
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
}