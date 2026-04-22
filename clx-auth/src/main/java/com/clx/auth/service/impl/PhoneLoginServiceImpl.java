package com.clx.auth.service.impl;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.clx.auth.entity.User;
import com.clx.auth.mapper.UserMapper;
import com.clx.auth.service.CaptchaService;
import com.clx.auth.service.PhoneLoginService;
import com.clx.auth.service.VerificationCodeService;
import com.clx.auth.vo.LoginVO;
import com.clx.common.core.constant.SecurityConstants;
import com.clx.common.core.constant.StatusConstants;
import com.clx.common.core.exception.AuthException;
import com.clx.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * 手机号登录服务实现
 *
 * <p>整体流程：
 * <ol>
 *   <li>发送验证码：验证图形验证码 → 生成短信验证码 → 存入Redis → 发送短信</li>
 *   <li>手机号登录：验证短信验证码 → 查找用户 → 没找到则创建 → 生成Token</li>
 * </ol>
 *
 * @author CLX
 * @since 2026-04-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneLoginServiceImpl implements PhoneLoginService {

    // ========== 依赖注入 ==========

    /**
     * 用户数据访问
     * 用途：根据手机号查询用户、创建新用户
     */
    private final UserMapper userMapper;

    /**
     * 验证码服务
     * 用途：验证图形验证码、存储和验证短信验证码
     */
    private final VerificationCodeService verificationCodeService;

    /**
     * 图形验证码服务
     * 用途：验证图形验证码（防刷）
     */
    private final CaptchaService captchaService;

    /**
     * sa-Token 配置
     * 用途：获取 Token 过期时间
     */
    private final SaTokenConfig saTokenConfig;

    /**
     * 密码加密器
     * 用途：为新用户生成随机密码
     */
    private final BCryptPasswordEncoder passwordEncoder;

    // ========== 发送验证码 ==========

    /**
     * 发送短信验证码
     *
     * <p>整体流程：
     * <ol>
     *   <li>验证图形验证码（防止短信接口被刷）</li>
     *   <li>生成 6 位数字验证码</li>
     *   <li>存入 Redis，5 分钟有效</li>
     *   <li>发送短信（开发环境直接打印日志）</li>
     * </ol>
     *
     * @param phone       手机号
     * @param captchaId   图形验证码ID
     * @param captchaCode 图形验证码
     */
    @Override
    public void sendSmsCode(String phone, String captchaId, String captchaCode) {
        log.info("发送短信验证码请求: phone={}", phone);

        // 步骤1：验证图形验证码（防刷）
        // 为什么需要？防止攻击者用脚本无限发送短信
        if (!captchaService.verifyCaptchaCode(captchaId, captchaCode)) {
            log.warn("图形验证码验证失败: phone={}", phone);
            throw AuthException.captchaError();
        }

        // 步骤2：生成 6 位纯数字验证码
        // 为什么是纯数字？短信验证码用户需要手动输入，数字更方便
        String smsCode = generateNumericCode(6);

        // 步骤3：存入 Redis，5 分钟有效
        // 返回 false 表示已存在，防止 5 分钟内重复发送
        if (!verificationCodeService.saveSmsCode(phone, smsCode)) {
            log.warn("验证码已发送，请勿重复请求: phone={}", phone);
            throw ServiceException.validationFailed("验证码已发送，5分钟内请勿重复请求");
        }

        // 步骤4：发送短信
        // 开发环境：直接打印日志，方便测试
        // 生产环境：对接阿里云/腾讯云短信服务
        log.info("=== 短信验证码已发送（开发环境）=== phone={}, code={}", phone, smsCode);

        // TODO: 生产环境对接短信服务商
        // sendSmsToProvider(phone, smsCode);
    }

    // ========== 手机号登录 ==========

    /**
     * 手机号登录
     *
     * <p>整体流程：
     * <ol>
     *   <li>验证短信验证码</li>
     *   <li>根据手机号查找用户</li>
     *   <li>找到 → 直接登录</li>
     *   <li>没找到 → 创建新用户 → 登录</li>
     *   <li>生成 sa-Token Token</li>
     * </ol>
     *
     * @param phone   手机号
     * @param smsCode 短信验证码
     * @return 登录结果，包含 Token
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(String phone, String smsCode) {
        log.info("手机号登录请求: phone={}", phone);

        // 步骤1：验证短信验证码
        // 从 Redis 取出验证码，对比是否一致
        if (!verificationCodeService.verifySmsCode(phone, smsCode)) {
            log.warn("短信验证码验证失败: phone={}", phone);
            throw AuthException.smsCodeError();
        }

        // 步骤2：根据手机号查找用户
        User user = userMapper.selectByPhone(phone);

        if (user == null) {
            // 用户不存在，创建新用户
            log.info("手机号未注册，创建新用户: phone={}", phone);
            user = createNewUser(phone);
        } else {
            // 用户已存在，检查状态
            if (user.isDisabled()) {
                throw AuthException.accountDisabled();
            }
            if (user.isLocked()) {
                throw AuthException.accountLocked();
            }
            log.info("手机号已注册，直接登录: phone={}, userId={}", phone, user.getUserId());
        }

        // 步骤3：生成 Token
        LoginVO loginVO = generateToken(user);

        log.info("手机号登录成功: userId={}, phone={}", user.getUserId(), phone);
        return loginVO;
    }

    // ========== 私有方法 ==========

    /**
     * 生成纯数字验证码
     *
     * @param length 验证码长度
     * @return 纯数字验证码
     */
    private String generateNumericCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 创建新用户
     *
     * <p>用于手机号首次登录时自动注册。
     *
     * @param phone 手机号
     * @return 新创建的用户
     */
    private User createNewUser(String phone) {
        // 生成用户ID
        Long userId = generateUserId();

        // 构造用户名：phone_ + 手机号后4位
        // 为什么这样命名？避免和已有用户名冲突，又能看出是手机号注册的
        String username = "phone_" + phone.substring(phone.length() - 4);

        // 如果用户名已存在，添加随机后缀
        int retry = 0;
        while (userMapper.existsByUsername(username) && retry < 10) {
            username = "phone_" + phone.substring(phone.length() - 4) + "_" + randomInt(1000);
            retry++;
        }

        // 生成随机密码
        // OAuth/手机号用户没有密码，生成一个随机密码
        // 用户后续可以通过邮箱绑定后重置密码
        String randomPassword = generateRandomPassword();

        // 创建用户对象
        User newUser = new User();
        newUser.setUserId(userId);
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(randomPassword));
        newUser.setNickname("用户" + phone.substring(phone.length() - 4));
        newUser.setPhone(phone);
        newUser.setStatus(StatusConstants.NORMAL);
        newUser.setIsDeleted(0);

        // 插入数据库
        int inserted = userMapper.insert(newUser);
        if (inserted <= 0) {
            log.error("创建用户失败: phone={}", phone);
            throw ServiceException.of(500, "用户创建失败");
        }

        log.info("新用户创建成功: userId={}, username={}, phone={}", userId, username, phone);
        return newUser;
    }

    /**
     * 生成用户ID
     *
     * <p>规则：时间戳（毫秒）* 1000 + 3位随机数
     *
     * @return 用户ID
     */
    private Long generateUserId() {
        long timestamp = System.currentTimeMillis();
        int random = randomInt(1000);
        return timestamp * 1000 + random;
    }

    /**
     * 生成随机整数
     *
     * @param max 最大值（不含）
     * @return 随机整数
     */
    private int randomInt(int max) {
        return new SecureRandom().nextInt(max);
    }

    /**
     * 生成随机密码
     *
     * <p>用于手机号登录用户，没有密码设置。
     *
     * @return 32位随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder(32);
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成登录 Token
     *
     * <p>使用 sa-Token 生成，与用户名密码登录保持一致。
     *
     * @param user 用户对象
     * @return 登录结果
     */
    private LoginVO generateToken(User user) {
        // 使用 sa-Token 登录
        StpUtil.login(user.getUserId(), SaLoginModel.create()
                .setTimeout(saTokenConfig.getTimeout())
                .setActiveTimeout(saTokenConfig.getActiveTimeout()));

        // 在 Session 中存储用户信息
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("nickname", user.getNickname());
        StpUtil.getSession().set("phone", user.getPhone());
        StpUtil.getSession().set("rememberMe", false);

        // 构造返回对象
        return new LoginVO(
                StpUtil.getTokenValue(),
                SecurityConstants.TOKEN_HEADER,
                saTokenConfig.getTimeout(),
                saTokenConfig.getActiveTimeout(),
                false
        );
    }
}