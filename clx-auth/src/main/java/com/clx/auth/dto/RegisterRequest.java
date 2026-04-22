package com.clx.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册请求 DTO。
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 128, message = "密码长度必须在8-128个字符之间")
        String password,

        @NotBlank(message = "确认密码不能为空")
        String confirmPassword,

        @Size(max = 50, message = "昵称长度不能超过50个字符")
        String nickname,

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,

        @NotBlank(message = "邮箱验证码不能为空")
        @Size(min = 6, max = 6, message = "邮箱验证码必须是6位")
        String emailCode
) {
}
