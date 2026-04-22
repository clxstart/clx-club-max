package com.clx.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 密码重置确认请求 DTO。
 *
 * <p>第二步：使用重置码设置新密码。
 */
public record PasswordResetConfirmRequest(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,

        @NotBlank(message = "重置码不能为空")
        String resetCode,

        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 128, message = "密码长度必须在8-128个字符之间")
        String newPassword,

        @NotBlank(message = "确认密码不能为空")
        String confirmPassword
) {}