package com.clx.common.core.constant;

/**
 * 安全相关常量
 */
public final class SecurityConstants {

    /** 用户ID请求头 */
    public static final String USER_ID_HEADER = "X-User-Id";

    /** 用户名请求头 */
    public static final String USERNAME_HEADER = "X-Username";

    /** 角色请求头 */
    public static final String ROLES_HEADER = "X-Roles";

    /** 权限请求头 */
    public static final String PERMISSIONS_HEADER = "X-Permissions";

    /** Token请求头 */
    public static final String TOKEN_HEADER = "Authorization";

    /** Token前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 内部请求来源 */
    public static final String FROM_SOURCE = "X-From-Source";

    /** 内部请求标识 */
    public static final String FROM_IN = "in";

    /** 超级管理员角色 */
    public static final String ADMIN_ROLE = "admin";

    /** 默认密码（新增用户） */
    public static final String DEFAULT_PASSWORD = "123456";

    /** BCrypt密码编码ID */
    public static final String BCRYPT_ID = "{bcrypt}";

    private SecurityConstants() {
    }

}