package com.clx.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 手机验证码请求 DTO。
 *
 * <p>用于发送手机验证码，需要图形验证码防刷。
 */
public record SmsCodeRequest(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @NotBlank(message = "图形验证码ID不能为空")
        String captchaId,

        @NotBlank(message = "图形验证码不能为空")
        String captchaCode
) {}