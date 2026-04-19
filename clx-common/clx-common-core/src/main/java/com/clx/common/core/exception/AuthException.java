package com.clx.common.core.exception;

import lombok.Getter;

/**
 * 认证异常。
 */
@Getter
public class AuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public AuthException(String message) {
        this(401, message);
    }

    public AuthException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static AuthException unauthorized() {
        return new AuthException(401, "请先登录");
    }

    public static AuthException invalidToken() {
        return new AuthException(401, "Token无效或已过期");
    }

    public static AuthException tokenExpired() {
        return new AuthException(401, "Token已过期，请重新登录");
    }

    public static AuthException loginFailed() {
        return new AuthException(401, "用户名或密码错误");
    }

    public static AuthException accountLocked() {
        return new AuthException(423, "账号已被锁定");
    }

    public static AuthException accountDisabled() {
        return new AuthException(403, "账号已被禁用");
    }

    public static AuthException passwordExpired() {
        return new AuthException(403, "密码已过期，请修改密码");
    }

    public static AuthException tooManyAttempts() {
        return new AuthException(429, "登录失败次数过多，请30分钟后再试");
    }
}
