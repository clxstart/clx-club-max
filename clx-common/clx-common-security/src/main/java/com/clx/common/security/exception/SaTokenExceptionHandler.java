package com.clx.common.security.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import com.clx.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * sa-Token 异常处理器。
 *
 * <p>统一处理 sa-Token 框架抛出的各类异常，返回规范的错误响应。
 *
 * <p>处理的异常类型：
 * <ul>
 *   <li>NotLoginException：未登录异常（Token无效、已过期、已被踢出等）</li>
 *   <li>NotPermissionException：无权限异常（缺少指定权限）</li>
 *   <li>NotRoleException：无角色异常（缺少指定角色）</li>
 *   <li>SaTokenException：其他 sa-Token 异常</li>
 * </ul>
 *
 * <p>响应格式：
 * <pre>
 * {
 *   "code": 401,
 *   "msg": "请先登录",
 *   "data": null,
 *   "timestamp": 1234567890,
 *   "success": false
 * }
 * </pre>
 *
 * <p>@Order(-1)：确保此处理器优先级高于其他全局异常处理器。
 *
 * @see R 统一响应对象
 */
@Slf4j
@RestControllerAdvice
@Order(-1)
public class SaTokenExceptionHandler {

    /**
     * 处理未登录异常。
     *
     * <p>触发场景：
     * <ul>
     *   <li>请求未携带Token</li>
     *   <li>Token已过期</li>
     *   <li>Token已被踢出（强制下线）</li>
     *   <li>Token已被替换（异地登录）</li>
     * </ul>
     *
     * @param e 未登录异常
     * @return 401响应，提示用户登录
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<R<Void>> handleNotLoginException(NotLoginException e) {
        log.warn("未登录异常: {}", e.getMessage());
        return ResponseEntity.status(401).body(R.fail(401, "请先登录"));
    }

    /**
     * 处理无权限异常。
     *
     * <p>触发场景：使用 @SaCheckPermission 注解，但用户缺少指定权限。
     *
     * <p>示例：
     * <pre>
     * &#64;SaCheckPermission("user:delete")
     * public void deleteUser(Long userId) { ... }
     * </pre>
     *
     * @param e 无权限异常
     * @return 403响应，提示无权限
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<R<Void>> handleNotPermissionException(NotPermissionException e) {
        log.warn("无权限异常: permission={}", e.getPermission());
        return ResponseEntity.status(403).body(R.fail(403, "没有权限访问"));
    }

    /**
     * 处理无角色异常。
     *
     * <p>触发场景：使用 @SaCheckRole 注解，但用户缺少指定角色。
     *
     * <p>示例：
     * <pre>
     * &#64;SaCheckRole("admin")
     * public void adminOnly() { ... }
     * </pre>
     *
     * @param e 无角色异常
     * @return 403响应，提示无权限
     */
    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<R<Void>> handleNotRoleException(NotRoleException e) {
        log.warn("无角色异常: role={}", e.getRole());
        return ResponseEntity.status(403).body(R.fail(403, "没有权限访问"));
    }

    /**
     * 处理其他 sa-Token 异常。
     *
     * <p>处理除上述异常外的其他 sa-Token 异常，
     * 如 Token 无效、被禁用等。
     *
     * @param e sa-Token异常
     * @return 401响应，提示认证失败
     */
    @ExceptionHandler(SaTokenException.class)
    public ResponseEntity<R<Void>> handleSaTokenException(SaTokenException e) {
        log.warn("sa-Token异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(401).body(R.fail(401, "认证失败"));
    }
}