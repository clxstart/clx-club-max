package com.clx.auth.controller;

import com.clx.auth.dto.EmailCodeRequest;
import com.clx.auth.dto.LoginRequest;
import com.clx.auth.dto.PasswordResetConfirmRequest;
import com.clx.auth.dto.PasswordResetRequest;
import com.clx.auth.dto.RegisterRequest;
import com.clx.auth.service.AuthService;
import com.clx.auth.support.CaptchaGenerator;
import com.clx.auth.support.CodeStorage;
import com.clx.auth.support.ClxMailSender;
import com.clx.auth.vo.LoginVO;
import com.clx.auth.vo.RegisterVO;
import com.clx.auth.vo.UserInfoVO;
import com.clx.common.core.domain.R;
import com.clx.common.core.exception.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 */
@Slf4j
@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaGenerator captchaGenerator;
    private final CodeStorage codeStorage;
    private final ClxMailSender mailSender;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String username = request.username() == null ? "" : request.username().trim();
        boolean rememberMe = Boolean.TRUE.equals(request.rememberMe());
        String clientIp = resolveClientIp(servletRequest);

        log.info("登录请求: username={}, ip={}", username, clientIp);
        return R.ok(authService.login(username, request.password(), request.captchaId(),
                request.captchaCode(), rememberMe, clientIp));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public R<RegisterVO> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        String username = request.username() == null ? "" : request.username().trim();
        String nickname = request.nickname() == null ? null : request.nickname().trim();
        String clientIp = resolveClientIp(servletRequest);

        log.info("注册请求: username={}, email={}", username, request.email());
        return R.ok(authService.register(username, request.password(), request.confirmPassword(),
                nickname, request.email(), request.emailCode(), clientIp));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户")
    public R<UserInfoVO> me() {
        return R.ok(authService.getCurrentUser());
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public R<LoginVO> refresh() {
        return R.ok(authService.refreshToken());
    }

    @PostMapping("/email-code/send")
    @Operation(summary = "发送邮箱验证码")
    public R<Void> sendEmailCode(@Valid @RequestBody EmailCodeRequest request) {
        String code = codeStorage.generate();

        if (!codeStorage.saveEmailCode(request.email(), code)) {
            throw ServiceException.validationFailed("验证码已发送，5分钟内请勿重复发送");
        }

        mailSender.sendCode(request.email(), code, "注册验证");
        log.info("邮箱验证码发送成功: email={}", request.email());
        return R.ok();
    }

    @PostMapping("/password-reset/send")
    @Operation(summary = "发送密码重置邮件")
    public R<Void> sendPasswordResetCode(@Valid @RequestBody PasswordResetRequest request) {
        if (!captchaGenerator.verify(request.captchaId(), request.captchaCode())) {
            throw ServiceException.validationFailed("图形验证码错误或已过期");
        }

        if (!authService.existsByEmail(request.email())) {
            log.warn("密码重置邮箱不存在: email={}", request.email());
            return R.ok();
        }

        String code = codeStorage.generate();
        codeStorage.saveResetCode(request.email(), code);
        mailSender.sendResetCode(request.email(), code);

        log.info("密码重置邮件发送成功: email={}", request.email());
        return R.ok();
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "确认密码重置")
    public R<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        if (!codeStorage.verifyResetCode(request.email(), request.resetCode())) {
            throw ServiceException.validationFailed("重置码错误或已过期");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw ServiceException.validationFailed("两次输入的密码不一致");
        }

        authService.resetPassword(request.email(), request.newPassword());
        log.info("密码重置成功: email={}", request.email());
        return R.ok();
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