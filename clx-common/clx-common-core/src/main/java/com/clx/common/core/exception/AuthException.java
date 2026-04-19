package com.clx.common.core.exception;

import lombok.Getter;

/**
 * 认证异常
 * 用于登录、Token验证等认证相关错误
 */
@Getter
public class AuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final int code;

    public AuthException(String message) {
        super(message);
        this.code = 401;
    }

    public AuthException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
        this.code = 401;
    }

    // ========== 常见异常静态工厂方法 ==========

    public static AuthException unauthorized() {
        return new AuthException("未授权，请先登录");
    }

    public static AuthException invalidToken() {
        return new AuthException("Token无效或已过期");
    }

    public static AuthException tokenExpired() {
        return new AuthException("Token已过期，请刷新");
    }

    public static AuthException loginFailed() {
        return new AuthException("用户名或密码错误");
    }

    public static AuthException accountLocked() {
        return new AuthException("账号已被锁定");
    }

    public static AuthException accountDisabled() {
        return new AuthException("账号已被禁用");
    }

    public static AuthException passwordExpired() {
        return new AuthException("密码已过期，请修改密码");
    }

    public static AuthException tooManyAttempts() {
        return new AuthException("登录失败次数过多，账号已锁定30分钟");
    }

}
