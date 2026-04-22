package com.clx.auth.service.impl;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.clx.auth.config.RememberMeProperties;
import com.clx.auth.entity.User;
import com.clx.auth.mapper.UserMapper;
import com.clx.auth.service.AuthService;
import com.clx.auth.service.CaptchaService;
import com.clx.auth.service.VerificationCodeService;
import com.clx.auth.vo.LoginVO;
import com.clx.auth.vo.RegisterVO;
import com.clx.auth.vo.UserInfoVO;
import com.clx.common.core.constant.SecurityConstants;
import com.clx.common.core.constant.TokenConstants;
import com.clx.common.core.exception.AuthException;
import com.clx.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$cX1Bgw3VdxwApyokYRF3B.iYYKD5IOu/8siinuC.M6NkQSIW7A4we";

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final SaTokenConfig saTokenConfig;
    private final RememberMeProperties rememberMeProperties;
    private final CaptchaService captchaService;
    private final VerificationCodeService verificationCodeService;

    @Override
    public LoginVO login(String username, String password, String captchaId, String captchaCode,
                         boolean rememberMe, String clientIp) {
        String normalizedUsername = normalizeUsername(username);
        String attemptKey = getAttemptKey(normalizedUsername);

        checkLoginLock(attemptKey);

        if (!captchaService.verifyCaptchaCode(captchaId, captchaCode)) {
            throw AuthException.captchaError();
        }

        User user = userMapper.selectByUsername(normalizedUsername);
        if (!isPasswordMatched(user, password)) {
            recordFailure(attemptKey);
            throw AuthException.loginFailed();
        }

        if (user.isDeleted()) {
            recordFailure(attemptKey);
            throw AuthException.loginFailed();
        }
        if (user.isDisabled()) {
            throw AuthException.accountDisabled();
        }
        if (user.isLocked()) {
            throw AuthException.accountLocked();
        }

        long loginTimeout = resolveLoginTimeout(rememberMe);
        long activeTimeout = resolveActiveTimeout(rememberMe, loginTimeout);

        StpUtil.login(user.getUserId(), SaLoginModel.create()
                .setTimeout(loginTimeout)
                .setActiveTimeout(activeTimeout)
                .setIsLastingCookie(rememberMe));

        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("nickname", user.getNickname());
        StpUtil.getSession().set("rememberMe", rememberMe);

        clearFailures(attemptKey);
        userMapper.updateLoginSuccess(user.getUserId(), clientIp);

        log.info("用户登录成功: username={}, userId={}, ip={}", user.getUsername(), user.getUserId(), clientIp);
        return new LoginVO(
                StpUtil.getTokenValue(),
                SecurityConstants.TOKEN_HEADER,
                loginTimeout,
                activeTimeout,
                rememberMe
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterVO register(String username, String password, String confirmPassword,
                               String nickname, String email, String emailCode, String clientIp) {
        String normalizedUsername = normalizeUsername(username);

        if (!verificationCodeService.verifyEmailCode(email, emailCode)) {
            throw AuthException.emailCodeError();
        }

        if (!password.equals(confirmPassword)) {
            throw ServiceException.validationFailed("两次输入的密码不一致");
        }

        if (userMapper.existsByUsername(normalizedUsername)) {
            throw ServiceException.alreadyExists("用户名");
        }

        if (userMapper.existsByEmail(email)) {
            throw ServiceException.alreadyExists("邮箱");
        }

        String encodedPassword = passwordEncoder.encode(password);
        Long userId = generateUserId();
        String finalNickname = (nickname == null || nickname.isBlank())
                ? normalizedUsername
                : nickname.trim();

        User user = new User();
        user.setUserId(userId);
        user.setUsername(normalizedUsername);
        user.setPassword(encodedPassword);
        user.setNickname(finalNickname);
        user.setEmail(email);

        int inserted = userMapper.insert(user);
        if (inserted <= 0) {
            throw new ServiceException(500, "用户创建失败");
        }

        long loginTimeout = saTokenConfig.getTimeout();
        long activeTimeout = saTokenConfig.getActiveTimeout();

        StpUtil.login(userId, SaLoginModel.create()
                .setTimeout(loginTimeout)
                .setActiveTimeout(activeTimeout));

        StpUtil.getSession().set("username", normalizedUsername);
        StpUtil.getSession().set("nickname", finalNickname);
        StpUtil.getSession().set("email", email);

        userMapper.updateLoginSuccess(userId, clientIp);
        log.info("用户注册成功: username={}, userId={}, email={}", normalizedUsername, userId, email);

        return new RegisterVO(
                userId,
                normalizedUsername,
                StpUtil.getTokenValue(),
                SecurityConstants.TOKEN_HEADER,
                loginTimeout,
                activeTimeout
        );
    }

    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    @Override
    public UserInfoVO getCurrentUser() {
        StpUtil.checkLogin();
        return new UserInfoVO(
                StpUtil.getLoginIdAsLong(),
                (String) StpUtil.getSession().get("username"),
                StpUtil.getTokenInfo()
        );
    }

    @Override
    public LoginVO refreshToken() {
        StpUtil.checkLogin();

        Long userId = StpUtil.getLoginIdAsLong();
        StpUtil.renewTimeout(saTokenConfig.getTimeout());

        Boolean rememberMe = (Boolean) StpUtil.getSession().get("rememberMe");
        boolean isRememberMe = Boolean.TRUE.equals(rememberMe);

        long timeout = isRememberMe ? rememberMeProperties.getTimeout() : saTokenConfig.getTimeout();
        long activeTimeout = isRememberMe ? rememberMeProperties.getActiveTimeout() : saTokenConfig.getActiveTimeout();

        log.info("Token 刷新成功: userId={}", userId);
        return new LoginVO(
                StpUtil.getTokenValue(),
                SecurityConstants.TOKEN_HEADER,
                timeout,
                activeTimeout,
                isRememberMe
        );
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String email, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        int updated = userMapper.updatePasswordByEmail(email, encodedPassword);
        if (updated <= 0) {
            throw ServiceException.notFound("邮箱对应的用户");
        }
        log.info("密码重置成功: email={}", email);
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isPasswordMatched(User user, String rawPassword) {
        String encodedPassword = user == null ? DUMMY_PASSWORD_HASH : user.getPassword();
        return passwordEncoder.matches(rawPassword, encodedPassword) && user != null;
    }

    private void checkLoginLock(String attemptKey) {
        Long count = readFailureCount(attemptKey);
        if (count >= TokenConstants.MAX_LOGIN_ATTEMPT) {
            throw AuthException.tooManyAttempts();
        }
    }

    private Long readFailureCount(String attemptKey) {
        try {
            String value = redisTemplate.opsForValue().get(attemptKey);
            if (value == null || value.isBlank()) {
                return 0L;
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            redisTemplate.delete(attemptKey);
            return 0L;
        } catch (RedisConnectionFailureException e) {
            throw new ServiceException(503, "认证缓存不可用，请稍后重试", e);
        }
    }

    private void recordFailure(String attemptKey) {
        try {
            Long count = redisTemplate.opsForValue().increment(attemptKey);
            if (count != null) {
                redisTemplate.expire(attemptKey, TokenConstants.LOGIN_LOCK_TIME, TimeUnit.SECONDS);
            }
        } catch (RedisConnectionFailureException e) {
            throw new ServiceException(503, "认证缓存不可用，请稍后重试", e);
        }
    }

    private void clearFailures(String attemptKey) {
        try {
            redisTemplate.delete(attemptKey);
        } catch (DataAccessException e) {
            log.warn("清理登录失败计数失败: key={}", attemptKey, e);
        }
    }

    private String getAttemptKey(String normalizedUsername) {
        return TokenConstants.LOGIN_ATTEMPT_KEY + normalizedUsername;
    }

    private long resolveLoginTimeout(boolean rememberMe) {
        return rememberMe ? rememberMeProperties.getTimeout() : saTokenConfig.getTimeout();
    }

    private long resolveActiveTimeout(boolean rememberMe, long loginTimeout) {
        long activeTimeout = rememberMe
                ? rememberMeProperties.getActiveTimeout()
                : saTokenConfig.getActiveTimeout();
        if (loginTimeout > 0 && activeTimeout > loginTimeout) {
            return loginTimeout;
        }
        return activeTimeout;
    }

    private Long generateUserId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return timestamp * 1000 + random;
    }
}
