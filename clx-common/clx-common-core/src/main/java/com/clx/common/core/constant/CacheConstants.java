package com.clx.common.core.constant;

/**
 * 缓存相关常量
 */
public final class CacheConstants {

    /** 用户信息缓存Key */
    public static final String USER_CACHE_KEY = "clx:user:";

    /** 角色缓存Key */
    public static final String ROLE_CACHE_KEY = "clx:role:";

    /** 权限缓存Key */
    public static final String PERMISSION_CACHE_KEY = "clx:permission:";

    /** 用户权限缓存Key */
    public static final String USER_PERM_CACHE_KEY = "clx:user_perm:";

    /** 验证码缓存Key */
    public static final String CAPTCHA_CODE_KEY = "clx:captcha:";

    /** 短信验证码缓存Key */
    public static final String SMS_CODE_KEY = "clx:sms:";

    /** 验证码有效期（秒） - 5分钟 */
    public static final long CAPTCHA_EXPIRATION = 300L;

    /** 短信验证码有效期（秒） - 5分钟 */
    public static final long SMS_CODE_EXPIRATION = 300L;

    private CacheConstants() {
    }

}