package com.clx.common.core.constant;

/**
 * Token相关常量
 */
public final class TokenConstants {

    /** Access Token Redis Key前缀 */
    public static final String ACCESS_TOKEN_KEY = "clx:auth:access:";

    /** Refresh Token Redis Key前缀 */
    public static final String REFRESH_TOKEN_KEY = "clx:auth:refresh:";

    /** 用户所有Token Redis Key前缀 */
    public static final String USER_TOKENS_KEY = "clx:auth:user_tokens:";

    /** 登录用户Redis Key前缀 */
    public static final String LOGIN_USER_KEY = "clx:auth:user:";

    /** 登录失败计数Key前缀 */
    public static final String LOGIN_ATTEMPT_KEY = "clx:auth:attempt:";

    /** Access Token有效期（秒） - 2小时 */
    public static final long ACCESS_TOKEN_EXPIRATION = 7200L;

    /** Refresh Token有效期（秒） - 7天 */
    public static final long REFRESH_TOKEN_EXPIRATION = 604800L;

    /** 登录失败最大次数 */
    public static final int MAX_LOGIN_ATTEMPT = 5;

    /** 登录失败锁定时间（秒） - 30分钟 */
    public static final long LOGIN_LOCK_TIME = 1800L;

    private TokenConstants() {
    }

}