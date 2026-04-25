package com.clx.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 当前用户信息
 */
@Schema(description = "当前用户信息")
public record UserInfoVO(
        @Schema(description = "用户ID")
        Long userId,
        @Schema(description = "用户名")
        String username,
        @Schema(description = "Token信息")
        Object tokenInfo
) {}