package com.clx.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 注册结果
 */
@Schema(description = "注册返回结果")
public record RegisterVO(
        @Schema(description = "用户ID")
        Long userId,
        @Schema(description = "用户名")
        String username,
        @Schema(description = "Token")
        String token,
        @Schema(description = "Token名称")
        String tokenName,
        @Schema(description = "绝对有效期（秒）")
        long tokenTimeout,
        @Schema(description = "活跃有效期（秒）")
        long activeTimeout
) {}