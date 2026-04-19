package com.clx.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Component;

/**
 * 认证中心启动自检器。
 *
 * <p>功能说明：
 * <ul>
 *   <li>在Spring Boot启动完成后自动执行</li>
 *   <li>检测Redis连接是否正常</li>
 *   <li>如果Redis不可用，阻止应用启动</li>
 * </ul>
 *
 * <p>设计原因：
 * Redis是sa-Token存储Token的核心组件，如果Redis不可用，
 * 用户登录后Token无法保存，会导致所有认证功能失效。
 * 因此在启动时强制检查Redis可用性，避免运行时出现不可预期的错误。
 *
 * @see ApplicationRunner Spring Boot启动后执行的接口
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthStartupValidator implements ApplicationRunner {

    /** Redis操作模板，用于执行Redis命令 */
    private final StringRedisTemplate redisTemplate;

    /**
     * 启动后执行的自检逻辑。
     *
     * @param args Spring Boot启动参数
     * @throws IllegalStateException 如果Redis不可用，抛出异常阻止启动
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            // 执行PING命令测试Redis连接
            redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
            log.info("认证缓存自检通过");
        } catch (DataAccessException e) {
            // Redis不可用，抛出异常阻止应用启动
            throw new IllegalStateException("认证中心启动失败：Redis 不可用，请先启动 Redis 或修正连接配置", e);
        }
    }
}