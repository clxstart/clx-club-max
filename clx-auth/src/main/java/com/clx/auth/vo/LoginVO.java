package com.clx.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录结果 VO。
 *
 * <p>登录成功后返回给前端的数据，包含 JWT Token 和有效期信息。
 *
 * <p>Token 格式说明：
 * <ul>
 *   <li>当前使用 JWT 格式（三段式 base64 字符串）</li>
 *   <li>JWT 包含用户ID、登录时间、随机字符串等信息</li>
 *   <li>网关可直接解析 JWT 获取用户ID，无需查 Redis</li>
 * </ul>
 *
 * <p>有效期说明：
 * <ul>
 *   <li>tokenTimeout：绝对有效期，Token 最大存活时间</li>
 *   <li>activeTimeout：活跃有效期，无操作后过期时间</li>
 *   <li>rememberMe=true 时，有效期更长（30天绝对，7天活跃）</li>
 * </ul>
 *
 * @param token JWT Token（三段式格式）
 * @param tokenName Token 名称（固定为 "Authorization"）
 * @param tokenTimeout Token 绝对有效期（秒）
 * @param activeTimeout Token 活跃有效期（秒）
 * @param rememberMe 是否开启了"记住我"
 */
@Schema(description = "登录返回结果")
public record LoginVO(
        @Schema(description = "JWT Token") String token,
        @Schema(description = "Token 名称") String tokenName,
        @Schema(description = "Token 绝对有效期（秒）") long tokenTimeout,
        @Schema(description = "Token 活跃有效期（秒）") long activeTimeout,
        @Schema(description = "是否开启记住我") boolean rememberMe
) {}