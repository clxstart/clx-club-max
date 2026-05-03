package com.clx.common.redis.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 辅助工具类。
 *
 * <p>封装常用 Redis 操作，简化调用代码。
 */
@Component
@RequiredArgsConstructor
public class RedisHelper {

    private final RedisTemplate<String, Object> redisTemplate;

    // ========== String操作 ==========

    /** 设置值 */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /** 设置值（带过期时间） */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /** 设置值（秒） */
    public void set(String key, Object value, long seconds) {
        set(key, value, seconds, TimeUnit.SECONDS);
    }

    /** 获取值 */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /** 获取值（泛型） */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        return value != null ? (T) value : null;
    }

    /** 删除 */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /** 批量删除 */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /** 是否存在 */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /** 设置过期时间 */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /** 设置过期时间（秒） */
    public Boolean expire(String key, long seconds) {
        return expire(key, seconds, TimeUnit.SECONDS);
    }

    /** 获取过期时间 */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /** 自增 */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /** 自增（指定步长） */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /** 自减 */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    // ========== Hash操作 ==========

    /** Hash设置 */
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /** Hash批量设置 */
    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /** Hash获取 */
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /** Hash获取所有 */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /** Hash删除 */
    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /** Hash是否存在 */
    public Boolean hHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    // ========== Set操作 ==========

    /** Set添加 */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /** Set获取所有 */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /** Set移除 */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /** Set大小 */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /** Set是否包含 */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    // ========== List操作 ==========

    /** List左侧添加 */
    public Long lPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /** List右侧添加 */
    public Long rPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /** List左侧弹出 */
    public Object lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /** List右侧弹出 */
    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /** List获取范围 */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /** List大小 */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // ========== 分布式锁操作 ==========

    /**
     * 尝试获取分布式锁。
     *
     * @param key       锁的 Key
     * @param value     锁的值（建议用 UUID）
     * @param expireSec 过期时间（秒）
     * @return 是否获取成功
     */
    public Boolean tryLock(String key, String value, long expireSec) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, expireSec, TimeUnit.SECONDS);
    }

    /**
     * 释放分布式锁。
     *
     * @param key   锁的 Key
     * @param value 锁的值（需一致）
     * @return 是否释放成功
     */
    public Boolean releaseLock(String key, String value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = redisTemplate.execute((RedisCallback<Long>) conn -> {
            Object r = conn.scriptingCommands().eval(
                    script.getBytes(),
                    org.springframework.data.redis.connection.ReturnType.INTEGER,
                    1,
                    key.getBytes(),
                    value.getBytes()
            );
            return ((Number) r).longValue();
        });
        return result != null && result > 0;
    }

    // ========== 位图操作 ==========

    /** 设置位图 */
    public Boolean setBit(String key, long offset, boolean value) {
        return redisTemplate.opsForValue().setBit(key, offset, value);
    }

    /** 获取位图 */
    public Boolean getBit(String key, long offset) {
        return redisTemplate.opsForValue().getBit(key, offset);
    }

    /** 统计位图 */
    public Long bitCount(String key) {
        return redisTemplate.execute((RedisCallback<Long>) conn -> {
            Object r = conn.bitCount(key.getBytes());
            return ((Number) r).longValue();
        });
    }
}
