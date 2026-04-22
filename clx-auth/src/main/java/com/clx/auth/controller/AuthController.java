package com.clx.auth.controller;

import com.clx.auth.dto.EmailCodeRequest;
import com.clx.auth.dto.LoginRequest;
import com.clx.auth.dto.PasswordResetConfirmRequest;
import com.clx.auth.dto.PasswordResetRequest;
import com.clx.auth.dto.RegisterRequest;
import com.clx.auth.dto.SmsCodeRequest;
import com.clx.auth.service.AuthService;
import com.clx.auth.service.CaptchaService;
import com.clx.auth.service.EmailService;
import com.clx.auth.service.VerificationCodeService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关接口。
 */
@Slf4j
@Tag(name = "认证接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;
    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String username = request.username() == null ? "" : request.username().trim();
        boolean rememberMe = Boolean.TRUE.equals(request.rememberMe());
        String clientIp = resolveClientIp(servletRequest);

        log.info("用户登录请求: username={}, ip={}, rememberMe={}", username, clientIp, rememberMe);
        return R.ok(authService.login(
                username,
                request.password(),
                request.captchaId(),
                request.captchaCode(),
                rememberMe,
                clientIp
        ));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public R<RegisterVO> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        String username = request.username() == null ? "" : request.username().trim();
        String nickname = request.nickname() == null ? null : request.nickname().trim();
        String clientIp = resolveClientIp(servletRequest);

        log.info("用户注册请求: username={}, email={}, ip={}", username, request.email(), clientIp);
        return R.ok(authService.register(
                username,
                request.password(),
                request.confirmPassword(),
                nickname,
                request.email(),
                request.emailCode(),
                clientIp
        ));
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

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token")
    public R<LoginVO> refresh() {
        return R.ok(authService.refreshToken());
    }

    @PostMapping("/email-code/send")
    @Operation(summary = "发送注册邮箱验证码")
    public R<Void> sendEmailCode(@Valid @RequestBody EmailCodeRequest request) {
        String code = verificationCodeService.generateCode();

        if (!verificationCodeService.saveEmailCode(request.email(), code)) {
            throw ServiceException.validationFailed("验证码已发送，5分钟内请勿重复发送");
        }

        emailService.sendVerificationCode(request.email(), code, "注册验证");
        log.info("注册邮箱验证码发送成功: email={}", request.email());
        return R.ok();
    }

    @PostMapping("/sms-code/send")
    @Operation(summary = "发送手机验证码")
    public R<String> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        if (!captchaService.verifyCaptchaCode(request.captchaId(), request.captchaCode())) {
            throw ServiceException.validationFailed("图形验证码错误或已过期");
        }

        String code = "123456";
        if (!verificationCodeService.saveSmsCode(request.phone(), code)) {
            throw ServiceException.validationFailed("验证码已发送，5分钟内请勿重复发送");
        }

        log.info("手机验证码发送成功: phone={}, code={}", request.phone(), code);
        return R.ok("验证码已发送（开发环境：123456）");
    }

    @PostMapping("/password-reset/send")
    @Operation(summary = "发送密码重置邮件")
    public R<Void> sendPasswordResetCode(@Valid @RequestBody PasswordResetRequest request) {
        if (!captchaService.verifyCaptchaCode(request.captchaId(), request.captchaCode())) {
            throw ServiceException.validationFailed("图形验证码错误或已过期");
        }

        if (!authService.existsByEmail(request.email())) {
            log.warn("密码重置请求邮箱不存在: email={}", request.email());
            return R.ok();
        }

        String code = verificationCodeService.generateCode();
        verificationCodeService.savePasswordResetCode(request.email(), code);
        emailService.sendPasswordResetCode(request.email(), code);

        log.info("密码重置邮件发送成功: email={}", request.email());
        return R.ok();
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "确认密码重置")
    public R<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        if (!verificationCodeService.verifyPasswordResetCode(request.email(), request.resetCode())) {
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
