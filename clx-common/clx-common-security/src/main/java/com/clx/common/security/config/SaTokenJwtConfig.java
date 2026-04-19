package com.clx.common.security.config;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * sa-Token JWT 整合配置。
 *
 * <p>模式说明：
 * sa-Token JWT 整合提供两种模式：
 *
 * <ul>
 *   <li><b>Simple 模式（当前使用）</b>：Token 格式为 JWT，但会话数据仍存储在 Redis 中。
 *       优点：保留踢人下线、账号封禁、权限刷新等完整功能。
 *       缺点：仍依赖 Redis。</li>
 *   <li><b>Stateless 模式</b>：Token 格式为 JWT，会话数据编码在 JWT 中，不依赖 Redis。
 *       优点：完全无状态，天然支持分布式。
 *       缺点：无法踢人下线、无法实时修改权限、Token 体积较大。</li>
 * </ul>
 *
 * <p>选择 Simple 模式的原因：
 * <ul>
 *   <li>项目已部署 Redis，无额外基础设施成本</li>
 *   <li>需要支持踢人下线、强制过期等功能</li>
 *   <li>JWT 格式便于网关层解析用户信息（无需查 Redis）</li>
 * </ul>
 *
 * @see StpLogicJwtForSimple Simple 模式的 StpLogic 实现
 */
@Configuration
public class SaTokenJwtConfig {

    /**
     * 注册 Sa-Token JWT Simple 模式的 StpLogic。
     *
     * <p>Sa-Token 默认使用 UUID Token，注册此 Bean 后，
     * Token 格式将自动切换为 JWT（三段式 base64 字符串）。
     *
     * <p>JWT Token 示例：
     * <pre>
     * eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpblR5cGUiOiJsb2dpbiIsImxvZ2luSWQiOjEsInJuU3RrIjoi...xxx
     * </pre>
     *
     * <p>其他代码无需任何修改，sa-Token 会自动适配 JWT Token 格式。
     *
     * @return JWT Simple 模式的 StpLogic 实例
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
}