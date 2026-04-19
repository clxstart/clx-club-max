package com.clx.auth.service.impl;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.clx.auth.config.RememberMeProperties;
import com.clx.auth.entity.User;
import com.clx.auth.mapper.UserMapper;
import com.clx.auth.service.AuthService;
import com.clx.auth.vo.LoginVO;
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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现。
 *
 * <p>核心功能：
 * <ul>
 *   <li>用户登录验证（用户名 + 密码）</li>
 *   <li>"记住我"功能（延长 Token 有效期）</li>
 *   <li>登录失败次数限制（防止暴力破解）</li>
 *   <li>用户登出</li>
 *   <li>获取当前登录用户信息</li>
 * </ul>
 *
 * <p>安全措施：
 * <ul>
 *   <li>时序攻击防护：用户不存在时也执行密码哈希比较</li>
 *   <li>错误信息统一：防止用户名枚举</li>
 *   <li>登录失败锁定：连续失败5次后锁定30分钟</li>
 *   <li>密码使用 BCrypt 加密存储</li>
 *   <li>Token 使用 JWT 格式（整合 sa-token-jwt）</li>
 * </ul>
 *
 * @see AuthService 认证服务接口
 * @see StpUtil sa-Token 用户操作工具
 * @see RememberMeProperties "记住我"配置
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /**
     * 假密码哈希值（时序攻击防护）。
     *
     * <p>当用户不存在时，使用此假密码执行 BCrypt 匹配，
     * 使响应时间与真实用户登录失败一致，
     * 攻击者无法通过响应时间判断用户是否存在。
     */
    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$cX1Bgw3VdxwApyokYRF3B.iYYKD5IOu/8siinuC.M6NkQSIW7A4we";

    /** 用户数据访问 */
    private final UserMapper userMapper;

    /** BCrypt 密码加密器 */
    private final BCryptPasswordEncoder passwordEncoder;

    /** Redis 操作模板（用于存储登录失败计数） */
    private final StringRedisTemplate redisTemplate;

    /** sa-Token 配置（获取默认超时时间） */
    private final SaTokenConfig saTokenConfig;

    /** "记住我"配置 */
    private final RememberMeProperties rememberMeProperties;

    /**
     * 用户登录。
     *
     * <p>处理流程：
     * <ol>
     *   <li>标准化用户名（去空格、小写）</li>
     *   <li>检查是否被锁定（连续失败5次）</li>
     *   <li>查询用户并验证密码（含时序攻击防护）</li>
     *   <li>检查用户状态（删除、禁用、锁定）</li>
     *   <li>根据 rememberMe 计算 Token 有效期</li>
     *   <li>调用 sa-Token 完成登录</li>
     *   <li>清除失败计数，更新登录信息</li>
     * </ol>
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @param rememberMe 是否勾选"记住我"
     * @param clientIp 客户端IP地址
     * @return 登录结果（JWT Token、有效期信息）
     * @throws AuthException 登录失败
     */
    @Override
    public LoginVO login(String username, String password, boolean rememberMe, String clientIp) {
        // 1. 标准化用户名
        String normalizedUsername = normalizeUsername(username);
        String attemptKey = getAttemptKey(normalizedUsername);

        // 2. 检查登录锁定状态
        checkLoginLock(attemptKey);

        // 3. 查询用户
        User user = userMapper.selectByUsername(normalizedUsername);

        // 4. 密码验证（含时序攻击防护）
        if (!isPasswordMatched(user, password)) {
            recordFailure(attemptKey);
            throw AuthException.loginFailed();
        }

        // 5. 检查用户状态
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

        // 6. 计算 Token 有效期
        long loginTimeout = resolveLoginTimeout(rememberMe);
        long activeTimeout = resolveActiveTimeout(rememberMe, loginTimeout);

        // 7. sa-Token 登录（JWT 格式）
        StpUtil.login(user.getUserId(), SaLoginModel.create()
                .setTimeout(loginTimeout)
                .setActiveTimeout(activeTimeout)
                .setIsLastingCookie(rememberMe));

        // 8. 存储会话信息
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("nickname", user.getNickname());

        // 9. 清除失败计数，更新登录信息
        clearFailures(attemptKey);
        userMapper.updateLoginSuccess(user.getUserId(), clientIp);

        log.info("用户登录成功: username={}, userId={}, ip={}, rememberMe={}, timeout={}, activeTimeout={}",
                user.getUsername(), user.getUserId(), clientIp, rememberMe, loginTimeout, activeTimeout);

        return new LoginVO(
                StpUtil.getTokenValue(),
                SecurityConstants.TOKEN_HEADER,
                loginTimeout,
                activeTimeout,
                rememberMe
        );
    }

    /**
     * 用户登出。
     *
     * <p>清除当前用户的会话信息，JWT Token 立即失效。
     */
    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    /**
     * 获取当前登录用户信息。
     *
     * @return 用户信息VO
     * @throws cn.dev33.satoken.exception.NotLoginException 如果用户未登录
     */
    @Override
    public UserInfoVO getCurrentUser() {
        StpUtil.checkLogin();
        return new UserInfoVO(
                StpUtil.getLoginIdAsLong(),
                (String) StpUtil.getSession().get("username"),
                StpUtil.getTokenInfo()
        );
    }

    /**
     * 标准化用户名（去空格、转小写）。
     */
    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 密码匹配验证（含时序攻击防护）。
     */
    private boolean isPasswordMatched(User user, String rawPassword) {
        String encodedPassword = user == null ? DUMMY_PASSWORD_HASH : user.getPassword();
        return passwordEncoder.matches(rawPassword, encodedPassword) && user != null;
    }

    /**
     * 检查登录锁定状态。
     */
    private void checkLoginLock(String attemptKey) {
        Long count = readFailureCount(attemptKey);
        if (count >= TokenConstants.MAX_LOGIN_ATTEMPT) {
            throw AuthException.tooManyAttempts();
        }
    }

    /**
     * 读取失败计数。
     */
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

    /**
     * 记录登录失败。
     */
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

    /**
     * 清除失败计数。
     */
    private void clearFailures(String attemptKey) {
        try {
            redisTemplate.delete(attemptKey);
        } catch (DataAccessException e) {
            log.warn("清理登录失败计数失败: key={}", attemptKey, e);
        }
    }

    /**
     * 构建失败计数的 Redis key。
     */
    private String getAttemptKey(String normalizedUsername) {
        return TokenConstants.LOGIN_ATTEMPT_KEY + normalizedUsername;
    }

    /**
     * 计算登录 Token 的绝对有效期。
     *
     * @param rememberMe 是否勾选"记住我"
     * @return 有效期（秒）
     */
    private long resolveLoginTimeout(boolean rememberMe) {
        return rememberMe ? rememberMeProperties.getTimeout() : saTokenConfig.getTimeout();
    }

    /**
     * 计算登录 Token 的活跃有效期。
     *
     * <p>活跃有效期不能超过绝对有效期。
     *
     * @param rememberMe 是否勾选"记住我"
     * @param loginTimeout 绝对有效期
     * @return 活跃有效期（秒）
     */
    private long resolveActiveTimeout(boolean rememberMe, long loginTimeout) {
        long activeTimeout = rememberMe
                ? rememberMeProperties.getActiveTimeout()
                : saTokenConfig.getActiveTimeout();
        // 活跃有效期不能超过绝对有效期
        if (loginTimeout > 0 && activeTimeout > loginTimeout) {
            return loginTimeout;
        }
        return activeTimeout;
    }
}