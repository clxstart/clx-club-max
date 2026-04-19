package com.clx.auth.service;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @return Token
     */
    String login(String username, String password);

    /**
     * 登出
     */
    void logout();

    /**
     * 获取当前登录用户ID
     */
    Long getLoginUserId();

    /**
     * 获取当前登录用户名
     */
    String getLoginUsername();

}
