package com.clx.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录结果
 */
@Schema(description = "登录返回结果")
public record LoginVO(
        @Schema(description = "Token")
        String token,
        @Schema(description = "Token名称")
        String tokenName,
        @Schema(description = "绝对有效期（秒）")
        long tokenTimeout,
        @Schema(description = "活跃有效期（秒）")
        long activeTimeout,
        @Schema(description = "记住我")
        boolean rememberMe
) {}