package com.clx.common.security.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import com.clx.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * sa-Token 异常处理器
 * 处理 sa-Token 相关的所有异常
 */
@Slf4j
@RestControllerAdvice
@Order(-1)  // 优先级高于 GlobalExceptionHandler
public class SaTokenExceptionHandler {

    /**
     * sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> handleNotLoginException(NotLoginException e) {
        log.warn("未登录异常: {}", e.getMessage());
        return R.fail(401, "请先登录");
    }

    /**
     * sa-Token 无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("无权限异常: permission={}", e.getPermission());
        return R.fail(403, "没有权限访问");
    }

    /**
     * sa-Token 无角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleNotRoleException(NotRoleException e) {
        log.warn("无角色异常: role={}", e.getRole());
        return R.fail(403, "没有权限访问");
    }

    /**
     * sa-Token 其他异常
     */
    @ExceptionHandler(SaTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> handleSaTokenException(SaTokenException e) {
        log.warn("sa-Token异常: code={}, message={}", e.getCode(), e.getMessage());
        return R.fail(401, "认证失败");
    }

}