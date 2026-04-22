package com.clx.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求 DTO。
 */
public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过50个字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(max = 128, message = "密码长度不能超过128个字符")
        String password,

        @NotBlank(message = "图形验证码ID不能为空")
        String captchaId,

        @NotBlank(message = "图形验证码不能为空")
        @Size(min = 4, max = 4, message = "图形验证码必须是4位")
        String captchaCode,

        Boolean rememberMe
) {
}
