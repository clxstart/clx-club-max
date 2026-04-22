package com.clx.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码管理服务。
 *
 * <p>封装Redis操作，统一管理邮箱验证码、手机验证码、密码重置码、图形验证码。
 *
 * <p>Key 设计：
 * <ul>
 *   <li>图形验证码：`captcha:{id}`</li>
 *   <li>邮箱验证码：`email:code:{email}`</li>
 *   <li>手机验证码：`sms:code:{phone}`</li>
 *   <li>密码重置码：`password:reset:{email}`</li>
 * </ul>
 *
 * <p>有效期：
 * <ul>
 *   <li>图形验证码：5 分钟</li>
 *   <li>邮箱/手机验证码：5 分钟</li>
 *   <li>密码重置码：30 分钟</li>
 * </ul>
 */
@Slf4j
@Service
public class VerificationCodeService {

    private final StringRedisTemplate redisTemplate;

    /** 验证码字符集（去除了易混淆的字符） */
    private static final String CODE_CHARS = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    /** 验证码长度 */
    private static final int CODE_LENGTH = 6;

    public VerificationCodeService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成随机验证码。
     *
     * @return 6位验证码
     */
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }

    /**
     * 保存图形验证码。
     *
     * @param captchaId 验证码ID（UUID）
     * @param code 验证码
     */
    public void saveCaptchaCode(String captchaId, String code) {
        String key = getCaptchaKey(captchaId);
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        log.debug("保存图形验证码: captchaId={}", captchaId);
    }

    /**
     * 验证图形验证码。
     *
     * @param captchaId 验证码ID
     * @param code 用户输入的验证码
     * @return true 如果验证码正确且未过期
     */
    public boolean verifyCaptchaCode(String captchaId, String code) {
        String key = getCaptchaKey(captchaId);
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("图形验证码已过期或不存在: captchaId={}", captchaId);
            return false;
        }

        boolean valid = storedCode.equals(code);
        if (valid) {
            redisTemplate.delete(key);
            log.info("图形验证码验证成功: captchaId={}", captchaId);
        } else {
            log.warn("图形验证码验证失败: captchaId={}", captchaId);
        }

        return valid;
    }

    /**
     * 保存邮箱验证码。
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @return false 如果已存在（防止重复发送）
     */
    public boolean saveEmailCode(String email, String code) {
        String key = getEmailCodeKey(email);

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            log.warn("邮箱验证码已存在，防止重复发送: email={}", email);
            return false;
        }

        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        log.info("保存邮箱验证码: email={}", email);
        return true;
    }

    /**
     * 验证邮箱验证码。
     *
     * @param email 邮箱地址
     * @param code 用户输入的验证码
     * @return true 如果验证码正确且未过期
     */
    public boolean verifyEmailCode(String email, String code) {
        String key = getEmailCodeKey(email);
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("邮箱验证码已过期或不存在: email={}", email);
            return false;
        }

        boolean valid = storedCode.equals(code);
        if (valid) {
            redisTemplate.delete(key);
            log.info("邮箱验证码验证成功: email={}", email);
        } else {
            log.warn("邮箱验证码验证失败: email={}", email);
        }

        return valid;
    }

    /**
     * 保存手机验证码。
     *
     * @param phone 手机号
     * @param code 验证码
     * @return false 如果已存在（防止重复发送）
     */
    public boolean saveSmsCode(String phone, String code) {
        String key = getSmsCodeKey(phone);

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            log.warn("手机验证码已存在，防止重复发送: phone={}", phone);
            return false;
        }

        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        log.info("保存手机验证码: phone={}", phone);
        return true;
    }

    /**
     * 验证手机验证码。
     *
     * @param phone 手机号
     * @param code 用户输入的验证码
     * @return true 如果验证码正确且未过期
     */
    public boolean verifySmsCode(String phone, String code) {
        String key = getSmsCodeKey(phone);
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("手机验证码已过期或不存在: phone={}", phone);
            return false;
        }

        boolean valid = storedCode.equals(code);
        if (valid) {
            redisTemplate.delete(key);
            log.info("手机验证码验证成功: phone={}", phone);
        } else {
            log.warn("手机验证码验证失败: phone={}", phone);
        }

        return valid;
    }

    /**
     * 保存密码重置码。
     *
     * @param email 邮箱地址
     * @param code 重置验证码
     */
    public void savePasswordResetCode(String email, String code) {
        String key = getPasswordResetKey(email);
        redisTemplate.opsForValue().set(key, code, 30, TimeUnit.MINUTES);
        log.info("保存密码重置码: email={}", email);
    }

    /**
     * 验证密码重置码。
     *
     * @param email 邮箱地址
     * @param code 用户输入的验证码
     * @return true 如果验证码正确且未过期
     */
    public boolean verifyPasswordResetCode(String email, String code) {
        String key = getPasswordResetKey(email);
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("密码重置码已过期或不存在: email={}", email);
            return false;
        }

        boolean valid = storedCode.equals(code);
        if (valid) {
            redisTemplate.delete(key);
            log.info("密码重置码验证成功: email={}", email);
        } else {
            log.warn("密码重置码验证失败: email={}", email);
        }

        return valid;
    }

    /**
     * 删除验证码。
     *
     * @param key Redis key
     */
    public void deleteCode(String key) {
        redisTemplate.delete(key);
    }

    // ========== Key 生成方法 ==========

    private String getCaptchaKey(String captchaId) {
        return "captcha:" + captchaId;
    }

    private String getEmailCodeKey(String email) {
        return "email:code:" + email;
    }

    private String getSmsCodeKey(String phone) {
        return "sms:code:" + phone;
    }

    private String getPasswordResetKey(String email) {
        return "password:reset:" + email;
    }
}
