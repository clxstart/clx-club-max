package com.clx.common.security.constant;

/**
 * 安全相关常量。
 *
 * <p>定义认证、授权、请求头等安全相关的常量值。
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

    /**
     * 默认密码（新增用户）。
     *
     * <p><b>安全警告</b>：此值为开发环境默认值。
     * 生产环境应通过配置文件覆盖，并强制要求用户首次登录修改密码。
     *
     * <p>配置方式：
     * <pre>
     * # application-prod.yml
     * security:
     *   default-password: ${DEFAULT_PASSWORD:随机生成的复杂密码}
     * </pre>
     */
    public static final String DEFAULT_PASSWORD = "123456";

    /** BCrypt密码编码ID */
    public static final String BCRYPT_ID = "{bcrypt}";

    private SecurityConstants() {
    }

}
