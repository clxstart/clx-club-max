package com.clx.common.core.constant;

/**
 * Token 相关常量。
 */
public final class TokenConstants {

    public static final String ACCESS_TOKEN_KEY = "clx:auth:access:";
    public static final String REFRESH_TOKEN_KEY = "clx:auth:refresh:";
    public static final String USER_TOKENS_KEY = "clx:auth:user_tokens:";
    public static final String LOGIN_USER_KEY = "clx:auth:user:";
    public static final String LOGIN_ATTEMPT_KEY = "clx:auth:attempt:";

    public static final long ACCESS_TOKEN_EXPIRATION = 14400L;
    public static final long ACTIVE_TOKEN_EXPIRATION = 7200L;
    public static final long REFRESH_TOKEN_EXPIRATION = 604800L;

    public static final int MAX_LOGIN_ATTEMPT = 5;
    public static final long LOGIN_LOCK_TIME = 1800L;

    private TokenConstants() {
    }
}
