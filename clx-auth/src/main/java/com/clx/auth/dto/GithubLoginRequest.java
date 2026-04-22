package com.clx.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * GitHub OAuth 登录请求 DTO
 * <p>
 * 用于接收前端传来的 GitHub 授权码，换取 access_token
 * <p>
 * 使用场景：
 * <ul>
 *   <li>用户点击 GitHub 登录按钮</li>
 *   <li>GitHub 授权完成后，前端拿到 code 传给后端</li>
 *   <li>后端用 code 换取 access_token 并登录</li>
 * </ul>
 *
 * @author CLX
 * @since 2026-04-22
 */
public record GithubLoginRequest(
        /**
         * GitHub 授权后返回的临时授权码
         * <p>
         * 有效期：10分钟
         * 使用次数：只能用一次
         * 获取方式：用户授权后从回调 URL 的 code 参数获取
         * <p>
         * 示例：
         * <pre>
         * https://your-app.com/auth/oauth/github/callback?code=xxx&state=yyy
         * </pre>
         *
         * @see <a href="https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps">GitHub OAuth 文档</a>
         */
        @NotBlank(message = "授权码不能为空")
        String code,

        /**
         * 防 CSRF 攻击的随机状态码
         * <p>
         * 生成方式：后端生成随机字符串，前端存储，回调时传回
         * 验证逻辑：后端对比 session 或 Redis 中存储的 state
         * <p>
         * 安全说明：
         * <ul>
         *   <li>必须使用随机字符串，不可预测</li>
         *   <li>验证失败时拒绝请求，防止 CSRF 攻击</li>
         *   <li>验证成功后立即删除，防止重用</li>
         * </ul>
         */
        @NotBlank(message = "状态码不能为空")
        String state
) {
}
