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
 * 启动自检器：应用启动后检测 Redis 是否可用，不可用则阻止启动。
 * 原因：sa-Token 的 Token 存储依赖 Redis，Redis 不可用会导致所有认证功能失效。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthStartupValidator implements ApplicationRunner {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
            log.info("认证缓存自检通过");
        } catch (DataAccessException e) {
            throw new IllegalStateException("认证中心启动失败：Redis 不可用", e);
        }
    }
}