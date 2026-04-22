package com.clx.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 注册结果 VO。
 *
 * <p>注册成功后返回给前端的数据，包含用户信息和 JWT Token。
 *
 * <p>注册成功后会自动登录，返回的 Token 可直接用于后续请求。
 *
 * @param userId 用户ID
 * @param username 用户名
 * @param token JWT Token
 * @param tokenName Token 名称（固定为 "Authorization"）
 * @param tokenTimeout Token 绝对有效期（秒）
 * @param activeTimeout Token 活跃有效期（秒）
 */
@Schema(description = "注册返回结果")
public record RegisterVO(
        @Schema(description = "用户ID")
        Long userId,

        @Schema(description = "用户名")
        String username,

        @Schema(description = "JWT Token")
        String token,

        @Schema(description = "Token 名称")
        String tokenName,

        @Schema(description = "Token 绝对有效期（秒）")
        long tokenTimeout,

        @Schema(description = "Token 活跃有效期（秒）")
        long activeTimeout
) {}