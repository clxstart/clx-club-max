package com.clx.common.core.code;

import lombok.Getter;

/**
 * 统一响应码枚举。
 *
 * <p>所有业务错误码统一在此定义，便于前后端同步。
 *
 * <p>错误码规则：
 * <ul>
 *   <li>200: 成功</li>
 *   <li>400-499: 客户端错误（参数错误、验证失败等）</li>
 *   <li>401: 未登录/Token失效</li>
 *   <li>403: 权限不足</li>
 *   <li>500-599: 服务端错误</li>
 *   <li>1000-1999: 认证模块错误</li>
 *   <li>2000-2999: 用户模块错误</li>
 *   <li>3000-3999: 帖子模块错误</li>
 * </ul>
 */
@Getter
public enum ResponseCode {

    // ========== 成功状态 ==========
    SUCCESS(200, "操作成功"),

    // ========== 通用客户端错误 400-499 ==========
    BAD_REQUEST(400, "请求参数错误"),
    VALIDATION_FAILED(400, "数据校验失败"),
    UNAUTHORIZED(401, "请先登录"),
    TOKEN_EXPIRED(401, "登录已过期，请重新登录"),
    TOKEN_INVALID(401, "Token无效"),
    FORBIDDEN(403, "没有权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源已存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),

    // ========== 服务端错误 500-599 ==========
    INTERNAL_ERROR(500, "系统繁忙，请稍后重试"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),
    DATABASE_ERROR(500, "数据库操作失败"),
    CACHE_ERROR(503, "缓存服务不可用"),

    // ========== 认证模块 1000-1999 ==========
    LOGIN_FAILED(1001, "用户名或密码错误"),
    ACCOUNT_DISABLED(1002, "账号已被禁用"),
    ACCOUNT_LOCKED(1003, "账号已被锁定"),
    ACCOUNT_NOT_FOUND(1004, "账号不存在"),
    TOO_MANY_LOGIN_ATTEMPTS(1005, "登录尝试过多，请稍后重试"),
    CAPTCHA_ERROR(1010, "图形验证码错误或已过期"),
    CAPTCHA_EXPIRED(1011, "图形验证码已过期"),
    EMAIL_CODE_ERROR(1012, "邮箱验证码错误或已过期"),
    EMAIL_CODE_EXPIRED(1013, "邮箱验证码已过期"),
    SMS_CODE_ERROR(1014, "短信验证码错误或已过期"),
    SMS_SEND_FAILED(1015, "短信发送失败"),
    EMAIL_SEND_FAILED(1016, "邮件发送失败"),
    CODE_ALREADY_SENT(1017, "验证码已发送，请勿重复请求"),
    PASSWORD_MISMATCH(1020, "两次密码输入不一致"),
    PASSWORD_TOO_SHORT(1021, "密码长度不足"),
    PASSWORD_TOO_LONG(1022, "密码长度超出限制"),
    USERNAME_EXISTS(1030, "用户名已存在"),
    EMAIL_EXISTS(1031, "邮箱已存在"),
    PHONE_EXISTS(1032, "手机号已存在"),
    USERNAME_INVALID(1033, "用户名格式不正确"),
    EMAIL_INVALID(1034, "邮箱格式不正确"),
    PHONE_INVALID(1035, "手机号格式不正确"),
    RESET_CODE_ERROR(1040, "密码重置码错误或已过期"),
    RESET_CODE_EXPIRED(1041, "密码重置码已过期"),

    // ========== 用户模块 2000-2999 ==========
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_DISABLED(2002, "用户已被禁用"),
    USER_LOCKED(2003, "用户已被锁定"),
    USER_ALREADY_EXISTS(2004, "用户已存在"),
    PROFILE_UPDATE_FAILED(2010, "个人资料更新失败"),
    AVATAR_UPDATE_FAILED(2011, "头像更新失败"),
    PASSWORD_UPDATE_FAILED(2012, "密码修改失败"),

    // ========== 帖子模块 3000-3999 ==========
    POST_NOT_FOUND(3001, "帖子不存在"),
    POST_DELETED(3002, "帖子已被删除"),
    POST_LOCKED(3003, "帖子已锁定"),
    POST_HIDDEN(3004, "帖子已隐藏"),
    POST_CREATE_FAILED(3010, "帖子发布失败"),
    POST_UPDATE_FAILED(3011, "帖子更新失败"),
    POST_DELETE_FAILED(3012, "帖子删除失败"),
    COMMENT_NOT_FOUND(3050, "评论不存在"),

    // ========== 评论模块 3100-3199 ==========
    COMMENT_CREATE_FAILED(3101, "评论发布失败"),
    COMMENT_DELETE_FAILED(3102, "评论删除失败"),

    // ========== 文件模块 4000-4099 ==========
    FILE_UPLOAD_FAILED(4001, "文件上传失败"),
    FILE_TOO_LARGE(4002, "文件大小超出限制"),
    FILE_TYPE_NOT_ALLOWED(4003, "文件类型不允许"),
    FILE_NOT_FOUND(4004, "文件不存在");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据 code 获取枚举。
     */
    public static ResponseCode fromCode(int code) {
        for (ResponseCode rc : values()) {
            if (rc.code == code) {
                return rc;
            }
        }
        return INTERNAL_ERROR;
    }

    /**
     * 判断是否成功。
     */
    public boolean isSuccess() {
        return this.code == 200;
    }
}