package com.clx.auth.service;

import com.clx.auth.vo.LoginVO;
import com.clx.auth.vo.RegisterVO;
import com.clx.auth.vo.UserInfoVO;

/**
 * 认证服务接口。
 */
public interface AuthService {

    /**
     * 用户登录。
     */
    LoginVO login(String username, String password, String captchaId, String captchaCode, boolean rememberMe, String clientIp);

    /**
     * 用户注册。
     */
    RegisterVO register(String username, String password, String confirmPassword, String nickname,
                        String email, String emailCode, String clientIp);

    /**
     * 用户登出。
     */
    void logout();

    /**
     * 获取当前登录用户信息。
     */
    UserInfoVO getCurrentUser();

    /**
     * 刷新 Token。
     */
    LoginVO refreshToken();

    /**
     * 检查邮箱是否存在。
     */
    boolean existsByEmail(String email);

    /**
     * 重置密码。
     */
    void resetPassword(String email, String newPassword);
}
