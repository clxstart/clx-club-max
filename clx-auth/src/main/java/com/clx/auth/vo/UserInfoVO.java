package com.clx.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 当前用户信息VO。
 *
 * <p>通过 GET /auth/me 接口获取的当前登录用户信息。
 *
 * @param userId    用户ID
 * @param username  用户名
 * @param tokenInfo sa-Token的Token详细信息（包含过期时间、剩余有效期等）
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