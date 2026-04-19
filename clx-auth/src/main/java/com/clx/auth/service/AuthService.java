package com.clx.auth.service;

import com.clx.auth.vo.LoginVO;
import com.clx.auth.vo.UserInfoVO;

/**
 * 认证服务。
 */
public interface AuthService {

    LoginVO login(String username, String password, boolean rememberMe, String clientIp);

    void logout();

    UserInfoVO getCurrentUser();
}
