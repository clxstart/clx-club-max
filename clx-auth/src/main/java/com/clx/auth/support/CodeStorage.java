package com.clx.auth.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 验证码存储器（Redis）
 */
@Slf4j
@Component
public class CodeStorage {

    private static final String CODE_CHARS = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;

    /** 复用 SecureRandom 实例 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redis;

    public CodeStorage(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 生成 6 位验证码 */
    public String generate() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(SECURE_RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    /** 存储邮箱验证码（5分钟有效），返回 false 表示已存在 */
    public boolean saveEmailCode(String email, String code) {
        String key = "email:code:" + email;
        if (Boolean.TRUE.equals(redis.hasKey(key))) {
            log.warn("邮箱验证码已存在: email={}", email);
            return false;
        }
        redis.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        log.info("存储邮箱验证码: email={}", email);
        return true;
    }

    /** 验证邮箱验证码 */
    public boolean verifyEmailCode(String email, String code) {
        return verify("email:code:" + email, email, code);
    }

    /** 存储短信验证码（5分钟有效），返回 false 表示已存在 */
    public boolean saveSmsCode(String phone, String code) {
        String key = "sms:code:" + phone;
        if (Boolean.TRUE.equals(redis.hasKey(key))) {
            log.warn("短信验证码已存在: phone={}", phone);
            return false;
        }
        redis.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        log.info("存储短信验证码: phone={}", phone);
        return true;
    }

    /** 验证短信验证码 */
    public boolean verifySmsCode(String phone, String code) {
        return verify("sms:code:" + phone, phone, code);
    }

    /** 存储密码重置码（30分钟有效） */
    public void saveResetCode(String email, String code) {
        redis.opsForValue().set("password:reset:" + email, code, 30, TimeUnit.MINUTES);
        log.info("存储密码重置码: email={}", email);
    }

    /** 验证密码重置码 */
    public boolean verifyResetCode(String email, String code) {
        return verify("password:reset:" + email, email, code);
    }

    private boolean verify(String key, String target, String code) {
        String stored = redis.opsForValue().get(key);
        if (stored == null) {
            log.warn("验证码已过期: {}", target);
            return false;
        }
        boolean valid = stored.equals(code);
        if (valid) {
            redis.delete(key);
            log.info("验证码验证成功: {}", target);
        } else {
            log.warn("验证码验证失败: {}", target);
        }
        return valid;
    }
}