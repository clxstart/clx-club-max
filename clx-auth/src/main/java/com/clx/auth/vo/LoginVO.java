package com.clx.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录结果VO。
 *
 * <p>登录成功后返回给前端的数据，包含Token和Token名称。
 *
 * <p>使用方式：
 * <pre>
 * // 前端获取Token后，在后续请求中携带：
 * Authorization: Bearer {token}
 * </pre>
 *
 * @param token     sa-Token生成的UUID Token
 * @param tokenName Token对应的Header名称（固定为"Authorization"）
 */
@Schema(description = "登录返回结果")
public record
LoginVO(
        @Schema(description = "Token") String token,
        @Schema(description = "Token名称") String tokenName
) {}