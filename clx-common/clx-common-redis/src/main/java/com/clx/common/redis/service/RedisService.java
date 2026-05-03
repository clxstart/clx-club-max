package com.clx.common.redis.service;

import com.clx.common.redis.helper.RedisHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Redis 服务层封装。
 *
 * <p>提供业务层直接使用的 Redis 操作方法。
 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisHelper redisHelper;

    /** 自增 */
    public Long increment(String key) {
        return redisHelper.increment(key);
    }

    /** 自增（指定步长） */
    public Long increment(String key, long delta) {
        return redisHelper.increment(key, delta);
    }

    /** 自减 */
    public Long decrement(String key) {
        return redisHelper.decrement(key);
    }

    /** 获取值 */
    public Object get(String key) {
        return redisHelper.get(key);
    }

    /** 获取值（泛型） */
    public <T> T get(String key, Class<T> clazz) {
        return redisHelper.get(key, clazz);
    }

    /** 设置值 */
    public void set(String key, Object value) {
        redisHelper.set(key, value);
    }

    /** 设置值（带过期时间） */
    public void set(String key, Object value, long seconds) {
        redisHelper.set(key, value, seconds);
    }

    /** 删除 */
    public Boolean delete(String key) {
        return redisHelper.delete(key);
    }

    /** 是否存在 */
    public Boolean hasKey(String key) {
        return redisHelper.hasKey(key);
    }

    /** 设置过期时间 */
    public Boolean expire(String key, long seconds) {
        return redisHelper.expire(key, seconds);
    }

    /** Hash设置 */
    public void hSet(String key, String hashKey, Object value) {
        redisHelper.hSet(key, hashKey, value);
    }

    /** Hash获取 */
    public Object hGet(String key, String hashKey) {
        return redisHelper.hGet(key, hashKey);
    }

    /** Hash获取所有 */
    public java.util.Map<Object, Object> hGetAll(String key) {
        return redisHelper.hGetAll(key);
    }

    /** Hash删除 */
    public Long hDelete(String key, Object... hashKeys) {
        return redisHelper.hDelete(key, hashKeys);
    }

    /** 尝试获取分布式锁 */
    public Boolean tryLock(String key, String value, long expireSec) {
        return redisHelper.tryLock(key, value, expireSec);
    }

    /** 释放分布式锁 */
    public Boolean releaseLock(String key, String value) {
        return redisHelper.releaseLock(key, value);
    }
}
