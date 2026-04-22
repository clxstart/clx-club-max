package com.clx.common.core.exception;

import com.clx.common.core.code.ResponseCode;
import lombok.Getter;

/**
 * 认证异常。
 *
 * <p>使用 ResponseCode 枚举确保错误码统一。
 */
@Getter
public class AuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public AuthException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }

    public AuthException(ResponseCode responseCode, String customMessage) {
        super(customMessage);
        this.code = responseCode.getCode();
    }

    public AuthException(int code, String message) {
        super(message);
        this.code = code;
    }

    // ========== 静态工厂方法 ==========

    public static AuthException unauthorized() {
        return new AuthException(ResponseCode.UNAUTHORIZED);
    }

    public static AuthException invalidToken() {
        return new AuthException(ResponseCode.TOKEN_INVALID);
    }

    public static AuthException tokenExpired() {
        return new AuthException(ResponseCode.TOKEN_EXPIRED);
    }

    public static AuthException loginFailed() {
        return new AuthException(ResponseCode.LOGIN_FAILED);
    }

    public static AuthException accountLocked() {
        return new AuthException(ResponseCode.ACCOUNT_LOCKED);
    }

    public static AuthException accountDisabled() {
        return new AuthException(ResponseCode.ACCOUNT_DISABLED);
    }

    public static AuthException tooManyAttempts() {
        return new AuthException(ResponseCode.TOO_MANY_LOGIN_ATTEMPTS);
    }

    public static AuthException captchaError() {
        return new AuthException(ResponseCode.CAPTCHA_ERROR);
    }

    public static AuthException emailCodeError() {
        return new AuthException(ResponseCode.EMAIL_CODE_ERROR);
    }
}