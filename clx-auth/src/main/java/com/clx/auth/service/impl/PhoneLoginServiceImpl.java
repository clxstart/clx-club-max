package com.clx.auth.service.impl;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.clx.auth.entity.User;
import com.clx.auth.mapper.UserMapper;
import com.clx.auth.service.PhoneLoginService;
import com.clx.auth.support.CaptchaGenerator;
import com.clx.auth.support.CodeStorage;
import com.clx.auth.vo.LoginVO;
import com.clx.common.core.constant.StatusConstants;
import com.clx.common.core.exception.AuthException;
import com.clx.common.core.exception.ServiceException;
import com.clx.common.security.constant.SecurityConstants;
import com.clx.common.core.util.SnowflakeIdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 手机号登录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneLoginServiceImpl implements PhoneLoginService {

    /** 复用 SecureRandom 实例 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** 短信验证码格式：6 位数字 */
    private static final String SMS_CODE_PATTERN = "^\\d{6}$";

    /** 登录失败次数上限 */
    private static final int MAX_LOGIN_FAILS = 5;

    /** 登录失败锁定时间（分钟） */
    private static final int LOGIN_FAIL_LOCK_MINUTES = 30;

    private final UserMapper userMapper;
    private final CodeStorage codeStorage;
    private final CaptchaGenerator captchaGenerator;
    private final SaTokenConfig saTokenConfig;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate redis;

    @Override
    public void sendSmsCode(String phone, String captchaId, String captchaCode) {
        log.info("发送短信验证码: phone={}", phone);

        if (!captchaGenerator.verify(captchaId, captchaCode)) {
            log.warn("图形验证码验证失败: phone={}", phone);
            throw AuthException.captchaError();
        }

        String smsCode = generateNumericCode(6);

        if (!codeStorage.saveSmsCode(phone, smsCode)) {
            log.warn("验证码已发送: phone={}", phone);
            throw ServiceException.validationFailed("验证码已发送，5分钟内请勿重复请求");
        }

        log.info("=== 短信验证码（开发环境）=== phone={}, code={}", phone, smsCode);
    }

    private String generateNumericCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(SECURE_RANDOM.nextInt(10));
        }
        return code.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(String phone, String smsCode) {
        log.info("手机号登录: phone={}", phone);

        // 验证码格式校验
        if (smsCode == null || !smsCode.matches(SMS_CODE_PATTERN)) {
            log.warn("短信验证码格式错误: phone={}, smsCode={}", phone, smsCode);
            throw AuthException.smsCodeError();
        }

        // 登录失败次数检查
        String failKey = "sms:fail:" + phone;
        String failCount = redis.opsForValue().get(failKey);
        if (failCount != null && Integer.parseInt(failCount) >= MAX_LOGIN_FAILS) {
            log.warn("登录失败次数过多: phone={}, fails={}", phone, failCount);
            throw ServiceException.of(429, "验证码错误次数过多，请" + LOGIN_FAIL_LOCK_MINUTES + "分钟后再试");
        }

        if (!codeStorage.verifySmsCode(phone, smsCode)) {
            log.warn("短信验证码验证失败: phone={}", phone);
            // 记录失败次数
            redis.opsForValue().increment(failKey);
            redis.expire(failKey, LOGIN_FAIL_LOCK_MINUTES, TimeUnit.MINUTES);
            throw AuthException.smsCodeError();
        }

        // 验证成功，清除失败记录
        redis.delete(failKey);

        User user = userMapper.selectByPhone(phone);

        if (user == null) {
            log.info("手机号未注册，创建新用户: phone={}", phone);
            user = createNewUser(phone);
        } else {
            if (user.isDisabled()) {
                throw AuthException.accountDisabled();
            }
            if (user.isLocked()) {
                throw AuthException.accountLocked();
            }
            log.info("手机号已注册，直接登录: phone={}, userId={}", phone, user.getUserId());
        }

        LoginVO loginVO = generateToken(user);
        log.info("手机号登录成功: userId={}, phone={}", user.getUserId(), phone);
        return loginVO;
    }

    private User createNewUser(String phone) {
        // 使用雪花算法生成 userId
        Long userId = SnowflakeIdWorker.genId();
        String username = "phone_" + phone.substring(phone.length() - 4);

        int retry = 0;
        while (userMapper.existsByUsername(username) && retry < 10) {
            username = "phone_" + phone.substring(phone.length() - 4) + "_" + SECURE_RANDOM.nextInt(1000);
            retry++;
        }

        // 重试 10 次仍未成功，抛出异常
        if (userMapper.existsByUsername(username)) {
            log.error("用户名生成失败，达到重试上限: phone={}", phone);
            throw ServiceException.of(500, "用户名生成失败，请稍后重试");
        }

        String randomPassword = generateRandomPassword();

        User newUser = new User();
        newUser.setUserId(userId);
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(randomPassword));
        newUser.setNickname("用户" + phone.substring(phone.length() - 4));
        newUser.setPhone(phone);
        newUser.setStatus(StatusConstants.NORMAL);
        newUser.setIsDeleted(0);

        int inserted = userMapper.insert(newUser);
        if (inserted <= 0) {
            log.error("创建用户失败: phone={}", phone);
            throw ServiceException.of(500, "用户创建失败");
        }

        log.info("新用户创建成功: userId={}, username={}, phone={}", userId, username, phone);
        return newUser;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private LoginVO generateToken(User user) {
        StpUtil.login(user.getUserId(), SaLoginModel.create()
                .setTimeout(saTokenConfig.getTimeout())
                .setActiveTimeout(saTokenConfig.getActiveTimeout()));

        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("nickname", user.getNickname());
        StpUtil.getSession().set("phone", user.getPhone());
        StpUtil.getSession().set("rememberMe", false);

        return new LoginVO(StpUtil.getTokenValue(), SecurityConstants.TOKEN_HEADER,
                saTokenConfig.getTimeout(), saTokenConfig.getActiveTimeout(), false);
    }
}