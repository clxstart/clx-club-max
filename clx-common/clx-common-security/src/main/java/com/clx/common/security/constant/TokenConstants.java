package com.clx.common.security.constant;

/**
 * Token 相关常量。
 *
 * <p>定义 Token 存储、过期时间、登录限制等相关常量。
 */
public final class TokenConstants {

    /** AccessToken Redis Key 前缀 */
    public static final String ACCESS_TOKEN_KEY = "clx:auth:access:";

    /** RefreshToken Redis Key 前缀 */
    public static final String REFRESH_TOKEN_KEY = "clx:auth:refresh:";

    /** 用户 Token 列表 Redis Key 前缀 */
    public static final String USER_TOKENS_KEY = "clx:auth:user_tokens:";

    /** 登录用户信息 Redis Key 前缀 */
    public static final String LOGIN_USER_KEY = "clx:auth:user:";

    /** 登录尝试次数 Redis Key 前缀 */
    public static final String LOGIN_ATTEMPT_KEY = "clx:auth:attempt:";

    /** AccessToken 有效期（秒）- 4小时 */
    public static final long ACCESS_TOKEN_EXPIRATION = 14400L;

    /** 活跃超时时间（秒）- 2小时 */
    public static final long ACTIVE_TOKEN_EXPIRATION = 7200L;

    /** RefreshToken 有效期（秒）- 7天 */
    public static final long REFRESH_TOKEN_EXPIRATION = 604800L;

    /** 最大登录尝试次数 */
    public static final int MAX_LOGIN_ATTEMPT = 5;

    /** 登录锁定时间（秒）- 30分钟 */
    public static final long LOGIN_LOCK_TIME = 1800L;

    private TokenConstants() {
    }
}
