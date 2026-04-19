package com.clx.auth.controller;

import com.clx.auth.dto.LoginRequest;
import com.clx.auth.service.AuthService;
import com.clx.auth.vo.LoginVO;
import com.clx.auth.vo.UserInfoVO;
import com.clx.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 */
@Slf4j
@Tag(name = "认证接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String username = request.username() == null ? "" : request.username().trim();
        boolean rememberMe = Boolean.TRUE.equals(request.rememberMe());
        String clientIp = resolveClientIp(servletRequest);

        log.info("用户登录请求: username={}, ip={}, rememberMe={}", username, clientIp, rememberMe);
        return R.ok(authService.login(username, request.password(), rememberMe, clientIp));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户")
    public R<UserInfoVO> me() {
        return R.ok(authService.getCurrentUser());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
