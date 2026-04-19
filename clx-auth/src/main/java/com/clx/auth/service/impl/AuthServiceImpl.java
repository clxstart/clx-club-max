package com.clx.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
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
 *   <li>登录失败次数限制（防止暴力破解）</li>
 *   <li>用户登出</li>
 *   <li>获取当前登录用户信息</li>
 * </ul>
 *
 * <p>安全措施：
 * <ul>
 *   <li>时序攻击防护：用户不存在时也执行密码哈希比较，避免通过响应时间判断用户是否存在</li>
 *   <li>错误信息统一：登录失败统一返回"用户名或密码错误"，防止用户名枚举</li>
 *   <li>登录失败锁定：连续失败5次后锁定30分钟</li>
 *   <li>密码使用BCrypt加密存储</li>
 * </ul>
 *
 * @see AuthService 认证服务接口
 * @see StpUtil sa-Token用户操作工具
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /**
     * 假密码哈希值。
     *
     * <p>用于时序攻击防护：当用户不存在时，使用此假密码执行BCrypt匹配，
     * 使响应时间与真实用户登录失败一致，攻击者无法通过响应时间判断用户是否存在。
     */
    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$cX1Bgw3VdxwApyokYRF3B.iYYKD5IOu/8siinuC.M6NkQSIW7A4we";

    /** 用户数据访问 */
    private final UserMapper userMapper;

    /** BCrypt密码加密器（Spring Bean，避免每次登录创建新实例） */
    private final BCryptPasswordEncoder passwordEncoder;

    /** Redis操作模板（用于存储登录失败计数） */
    private final StringRedisTemplate redisTemplate;

    /**
     * 用户登录。
     *
     * <p>处理流程：
     * <ol>
     *   <li>标准化用户名（去空格、小写）</li>
     *   <li>检查是否被锁定（连续失败5次）</li>
     *   <li>查询用户并验证密码（含时序攻击防护）</li>
     *   <li>检查用户状态（删除、禁用、锁定）</li>
     *   <li>调用sa-Token完成登录</li>
     *   <li>清除失败计数，更新登录信息</li>
     * </ol>
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @param clientIp 客户端IP地址
     * @return 登录结果（Token和Token名称）
     * @throws AuthException 登录失败（用户名或密码错误、账户禁用、账户锁定、尝试次数过多）
     */
    @Override
    public LoginVO login(String username, String password, String clientIp) {
        // 1. 标准化用户名：去空格、转小写
        String normalizedUsername = normalizeUsername(username);
        String attemptKey = getAttemptKey(normalizedUsername);

        // 2. 检查是否被锁定（连续失败达到上限）
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

        // 6. sa-Token登录：将用户ID存入会话
        StpUtil.login(user.getUserId());
        // 在会话中存储用户基本信息（方便后续获取）
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("nickname", user.getNickname());

        // 7. 清除失败计数，更新登录成功信息
        clearFailures(attemptKey);
        userMapper.updateLoginSuccess(user.getUserId(), clientIp);

        log.info("用户登录成功: username={}, userId={}, ip={}", user.getUsername(), user.getUserId(), clientIp);
        return new LoginVO(StpUtil.getTokenValue(), SecurityConstants.TOKEN_HEADER);
    }

    /**
     * 用户登出。
     *
     * <p>调用sa-Token的logout方法，清除当前用户的会话信息。
     * 如果用户未登录，则不做任何操作。
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
     * <p>先检查是否登录，然后从sa-Token会话中获取用户信息。
     *
     * @return 用户信息VO（用户ID、用户名、Token信息）
     * @throws NotLoginException 如果用户未登录
     */
    @Override
    public UserInfoVO getCurrentUser() {
        // 检查登录状态，未登录会抛出NotLoginException
        StpUtil.checkLogin();
        return new UserInfoVO(
                StpUtil.getLoginIdAsLong(),
                (String) StpUtil.getSession().get("username"),
                StpUtil.getTokenInfo()
        );
    }

    /**
     * 标准化用户名。
     *
     * <p>去除首尾空格并转为小写，避免大小写导致的用户名混淆。
     *
     * @param username 原始用户名
     * @return 标准化后的用户名
     */
    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 密码匹配验证（含时序攻击防护）。
     *
     * <p>时序攻击防护原理：
     * <ul>
     *   <li>如果用户不存在，使用假密码哈希执行BCrypt匹配</li>
     *   <li>BCrypt匹配耗时约100ms，无论密码是否正确</li>
     *   <li>攻击者无法通过响应时间判断用户是否存在</li>
     * </ul>
     *
     * @param user 用户对象（可能为null）
     * @param rawPassword 明文密码
     * @return 密码是否匹配（用户不存在时始终返回false）
     */
    private boolean isPasswordMatched(User user, String rawPassword) {
        // 如果用户不存在，使用假密码哈希执行匹配（时序攻击防护）
        String encodedPassword = user == null ? DUMMY_PASSWORD_HASH : user.getPassword();
        // 只有用户存在且密码匹配才返回true
        return passwordEncoder.matches(rawPassword, encodedPassword) && user != null;
    }

    /**
     * 检查登录锁定状态。
     *
     * <p>如果失败次数达到上限（5次），抛出异常阻止登录。
     *
     * @param attemptKey Redis中的失败计数key
     * @throws AuthException 如果尝试次数过多
     */
    private void checkLoginLock(String attemptKey) {
        Long count = readFailureCount(attemptKey);
        if (count >= TokenConstants.MAX_LOGIN_ATTEMPT) {
            throw AuthException.tooManyAttempts();
        }
    }

    /**
     * 读取失败计数。
     *
     * <p>从Redis中读取用户的登录失败次数。
     *
     * @param attemptKey Redis key
     * @return 失败次数（0表示无记录或解析失败）
     */
    private Long readFailureCount(String attemptKey) {
        try {
            String value = redisTemplate.opsForValue().get(attemptKey);
            if (value == null || value.isBlank()) {
                return 0L;
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            // 数值格式错误，删除key并返回0
            redisTemplate.delete(attemptKey);
            return 0L;
        } catch (RedisConnectionFailureException e) {
            // Redis不可用，抛出503错误
            throw new ServiceException(503, "认证缓存不可用，请稍后重试", e);
        }
    }

    /**
     * 记录登录失败。
     *
     * <p>在Redis中增加失败计数，并设置过期时间。
     *
     * @param attemptKey Redis key
     * @throws ServiceException 如果Redis不可用
     */
    private void recordFailure(String attemptKey) {
        try {
            // increment原子操作，返回增加后的值
            Long count = redisTemplate.opsForValue().increment(attemptKey);
            if (count != null) {
                // 设置过期时间（锁定时长）
                redisTemplate.expire(attemptKey, TokenConstants.LOGIN_LOCK_TIME, TimeUnit.SECONDS);
            }
        } catch (RedisConnectionFailureException e) {
            throw new ServiceException(503, "认证缓存不可用，请稍后重试", e);
        }
    }

    /**
     * 清除失败计数。
     *
     * <p>登录成功后删除Redis中的失败计数。
     * 失败时只记录警告日志，不影响登录成功。
     *
     * @param attemptKey Redis key
     */
    private void clearFailures(String attemptKey) {
        try {
            redisTemplate.delete(attemptKey);
        } catch (DataAccessException e) {
            // 清理失败不影响登录，只记录警告
            log.warn("清理登录失败计数失败: key={}", attemptKey, e);
        }
    }

    /**
     * 构建失败计数的Redis key。
     *
     * @param normalizedUsername 标准化后的用户名
     * @return Redis key，如 "login_attempt:admin"
     */
    private String getAttemptKey(String normalizedUsername) {
        return TokenConstants.LOGIN_ATTEMPT_KEY + normalizedUsername;
    }
}