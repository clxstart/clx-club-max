package com.clx.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 密码重置请求 DTO。
 *
 * <p>第一步：发送密码重置邮件，需要图形验证码防刷。
 */
public record PasswordResetRequest(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,

        @NotBlank(message = "图形验证码ID不能为空")
        String captchaId,

        @NotBlank(message = "图形验证码不能为空")
        String captchaCode
) {}