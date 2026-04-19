package com.clx.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clx.auth.entity.User;
import com.clx.auth.mapper.UserMapper;
import com.clx.auth.service.AuthService;
import com.clx.common.core.constant.StatusConstants;
import com.clx.common.core.constant.TokenConstants;
import com.clx.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public String login(String username, String password) {
        // 1. 检查登录失败次数（防暴力破解）
        String attemptKey = TokenConstants.LOGIN_ATTEMPT_KEY + username;
        String attemptCount = stringRedisTemplate.opsForValue().get(attemptKey);
        if (attemptCount != null && Integer.parseInt(attemptCount) >= TokenConstants.MAX_LOGIN_ATTEMPT) {
            log.warn("账号登录失败次数过多，已锁定: username={}", username);
            throw new ServiceException("登录失败次数过多，请" + (TokenConstants.LOGIN_LOCK_TIME / 60) + "分钟后再试");
        }

        // 2. 查询用户
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        // 3. 统一错误信息（防止用户枚举攻击）
        if (user == null) {
            recordLoginFailure(attemptKey);
            throw new ServiceException("用户名或密码错误");
        }

        // 4. 先检查账号状态（避免时序攻击：先状态后密码）
        if (StatusConstants.DISABLED.equals(user.getStatus())) {
            throw new ServiceException("账号已被禁用");
        }
        if (StatusConstants.LOCKED.equals(user.getStatus())) {
            throw new ServiceException("账号已被锁定");
        }

        // 5. 验证密码
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            recordLoginFailure(attemptKey);
            throw new ServiceException("用户名或密码错误");
        }

        // 6. 登录成功，清除失败计数
        stringRedisTemplate.delete(attemptKey);

        // 7. sa-Token 登录
        StpUtil.login(user.getUserId());
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("nickname", user.getNickname());

        log.info("用户登录成功: username={}, userId={}", username, user.getUserId());
        return StpUtil.getTokenValue();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public Long getLoginUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    @Override
    public String getLoginUsername() {
        return (String) StpUtil.getSession().get("username");
    }

    /**
     * 记录登录失败次数
     */
    private void recordLoginFailure(String attemptKey) {
        Long count = stringRedisTemplate.opsForValue().increment(attemptKey);
        if (count != null && count == 1) {
            // 第一次失败，设置过期时间
            stringRedisTemplate.expire(attemptKey, TokenConstants.LOGIN_LOCK_TIME, TimeUnit.SECONDS);
        }
    }

}