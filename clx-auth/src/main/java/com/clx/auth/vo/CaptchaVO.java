package com.clx.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 图形验证码
 */
@Schema(description = "图形验证码")
public record CaptchaVO(
        @Schema(description = "验证码ID")
        String id,
        @Schema(description = "Base64图片")
        String image
) {}